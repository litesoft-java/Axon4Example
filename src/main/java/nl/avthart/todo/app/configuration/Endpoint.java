package nl.avthart.todo.app.configuration;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
public class Endpoint {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Admin {
    }

    public interface Controller {
    }

    public Set<String> adminPathsFor( RequestMethod method ) {
        return pathsFor( method, true );
    }

    public Set<String> userPathsFor( RequestMethod method ) {
        return pathsFor( method, false );
    }

    private Set<String> pathsFor( RequestMethod method, boolean admin ) {
        if ( method == null ) {
            throw new IllegalArgumentException( "No Method provided" );
        }
        Set<String> paths = pathsByKey.get( new Key( method, admin ) );
        return (paths == null) ? Collections.emptySet() : Collections.unmodifiableSet( paths );
    }

    @Value
    private static class Key {
        RequestMethod method;
        boolean admin;
    }

    private final Map<Key, Set<String>> pathsByKey = new HashMap<>();

    public Endpoint( List<Controller> controllers ) {
        if ( (controllers != null) && !controllers.isEmpty() ) {
            for ( Controller c : controllers ) {
                if ( c != null ) {
                    process( c.getClass() );
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Endpoint{" +
               "pathsByKey=" + pathsByKey +
               '}';
    }

    private void process( Class<?> c ) {
        ControllerClass controller = new ControllerClass( c.getDeclaredAnnotations() );
        do {
            controller.process( c );
            c = c.getSuperclass();
        } while ( c != null );
    }

    private class ControllerClass {
        private final boolean admin;
        private final String basePath;

        public ControllerClass( Annotation[] annotations ) {
            GeneralInterestingAnnotations ia = new GeneralInterestingAnnotations();
            ia.process( annotations );
            this.admin = ia.admin;
            this.basePath = (ia.path == null) ? "" : ia.path;
        }

        public void process( Class<?> c ) {
            Method[] methods = c.getDeclaredMethods();
            for ( Method method : methods ) {
                if ( method != null ) {
                    process( c, method );
                }
            }
        }

        private void process( Class<?> c, Method method ) {
            MethodInterestingAnnotations ia = new MethodInterestingAnnotations();
            ia.process( method.getDeclaredAnnotations() );
            String path = ia.merge( basePath );
            if ( path != null ) {
                RequestMethod rm = ia.method;
                if ( rm == null ) {
                    System.out.println( c.getName() + "." + method.getName() + ", has path '" + path + "', but no RequestMethod" );
                } else {
                    Set<String> paths = pathsByKey.computeIfAbsent( new Key( rm, ia.merge( admin ) ), key -> new HashSet<>() );
                    paths.add( normalizePath( path ) );
                }
            }
        }
    }

    private static class GeneralInterestingAnnotations {
        private String path;
        private boolean admin;

        public void process( Annotation[] annotations ) {
            if ( (annotations != null) && (annotations.length != 0) ) {
                for ( Annotation annotation : annotations ) {
                    process( annotation );
                }
            }
        }

        public String merge( String basePath ) {
            return (path == null) ? null : (basePath + path);
        }

        public boolean merge( boolean admin ) {
            return (this.admin || admin);
        }

        private void process( Annotation annotation ) {
            if ( annotation.annotationType() == Admin.class ) {
                admin = true;
            } else if ( annotation.annotationType() == RequestMapping.class ) {
                process( (RequestMapping)annotation );
            } else if ( annotation.annotationType() == GetMapping.class ) {
                process( (GetMapping)annotation );
            } else if ( annotation.annotationType() == PostMapping.class ) {
                process( (PostMapping)annotation );
            } else if ( annotation.annotationType() == PutMapping.class ) {
                process( (PutMapping)annotation );
            } else if ( annotation.annotationType() == PatchMapping.class ) {
                process( (PatchMapping)annotation );
            } else if ( annotation.annotationType() == DeleteMapping.class ) {
                process( (DeleteMapping)annotation );
            }
        }

        protected void process( RequestMapping annotation ) {
            path( annotation.value(), annotation.path() );
        }

        protected void process( GetMapping annotation ) {
            path( annotation.value(), annotation.path() );
        }

        protected void process( PostMapping annotation ) {
            path( annotation.value(), annotation.path() );
        }

        protected void process( PutMapping annotation ) {
            path( annotation.value(), annotation.path() );
        }

        protected void process( PatchMapping annotation ) {
            path( annotation.value(), annotation.path() );
        }

        protected void process( DeleteMapping annotation ) {
            path( annotation.value(), annotation.path() );
        }

        private void path( String[] values, String[] paths ) {
            Set<String> merged = mergeValuesAndPaths( values, paths );
            for ( String path : merged ) {
                if ( (path != null) && !path.isEmpty() ) {
                    this.path = path;
                    return;
                }
            }
            path = "";
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class MethodInterestingAnnotations extends GeneralInterestingAnnotations {
        private RequestMethod method;

        @Override
        protected void process( RequestMapping annotation ) {
            super.process( annotation );
            RequestMethod[] methods = annotation.method();
            if ( (methods != null) && (methods.length != 0) ) {
                method = methods[0];
            }
        }

        @Override
        protected void process( GetMapping annotation ) {
            super.process( annotation );
            method = RequestMethod.GET;
        }

        @Override
        protected void process( PostMapping annotation ) {
            super.process( annotation );
            method = RequestMethod.POST;
        }

        @Override
        protected void process( PutMapping annotation ) {
            super.process( annotation );
            method = RequestMethod.PUT;
        }

        @Override
        protected void process( PatchMapping annotation ) {
            super.process( annotation );
            method = RequestMethod.PATCH;
        }

        @Override
        protected void process( DeleteMapping annotation ) {
            super.process( annotation );
            method = RequestMethod.DELETE;
        }
    }

    private static Set<String> mergeValuesAndPaths( String[] values, String[] paths ) {
        Set<String> combinedPaths = new HashSet<>();
        if ( (paths != null) && (paths.length != 0) ) {
            combinedPaths.addAll( Arrays.asList( paths ) );
        }
        if ( (values != null) && (values.length != 0) ) {
            combinedPaths.addAll( Arrays.asList( values ) );
        }
        return combinedPaths;
    }

    private static String normalizePath( String path ) {
        int closeAt = path.indexOf( '}' );
        if ( closeAt == -1 ) {
            return path;
        }
        StringBuilder sb = new StringBuilder( path );
        for ( int openAt; -1 != (openAt = sb.lastIndexOf( "{", closeAt )); ) {
            sb.delete( openAt, closeAt );
            sb.setCharAt( openAt, '*' );
            closeAt = path.indexOf( '}', openAt );
            if ( closeAt == -1 ) {
                break;
            }
        }
        return sb.toString();
    }
}
