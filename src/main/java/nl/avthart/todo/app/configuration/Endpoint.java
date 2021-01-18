package nl.avthart.todo.app.configuration;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class Endpoint {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Updated {
        String date() default ""; // yyyy-mm-dd
    }

    public interface Controller {
    }

    public interface Role {
        /**
         * Determine if this Role is the Default Role (also indicates that there is no Access Annotation for this Role).
         *
         * @return true if is Default Role
         */
        boolean isDefaultRole();

        /**
         * Used to map an Annotation (by its class/type) to a Role.
         *
         * @param accessAnnotationClass the Potential Annotation class or type
         * @return true if this Role is selected by the accessAnnotationClass parameter
         */
        boolean isAccessAnnotationClass( Class<? extends Annotation> accessAnnotationClass );

        /**
         * The other Roles that are implicitly implied by "this" Role.
         *
         * @return list of implied Roles, Not Null, but often empty!
         */
        @SuppressWarnings("unused")
        // Utilized later in the Spring's Web Security Configuration
        List<Role> impliedRoles();
    }

    public static class ProcessingException extends RuntimeException {
        private ProcessingException( String message, Throwable cause ) {
            super( message, cause );
        }

        private static ProcessingException from( Class<?> source, RuntimeException e ) {
            Throwable realCause = e;
            for ( Throwable cause = realCause.getCause(); cause != null && cause != e; cause = realCause.getCause() ) {
                realCause = cause;
            }
            return new ProcessingException( "On " + source.getSimpleName() + ", " + e.getMessage(), realCause );
        }
    }

    @SuppressWarnings("unused")
    public static Endpoint getCachedData() {
        return endpoint;
    }

    public static Endpoint from( List<Role> roles, List<Controller> controllers ) {
        if ( (roles == null) || roles.isEmpty() ) {
            throw new IllegalArgumentException( "No Roles?" );
        }
        if ( (controllers == null) || controllers.isEmpty() ) {
            throw new IllegalArgumentException( "No Controllers?" );
        }
        if ( (endpoint != null) && // Left to Right!
             (endpoint.controllerCount == controllers.size()) && (endpoint.roles.size() == roles.size()) ) {
            return endpoint;
        }
        System.out.println( "Endpoint Controller(s) Security Mappings being Generated" ); // NOSONAR
        endpoint = new Endpoint( roles, controllers );
        return endpoint;
    }

    public HttpMethod map( RequestMethod method ) {
        return HttpMethod.resolve( method.name() );
    }

    public RequestMethodPaths pathsFor( Role role ) {
        if ( role != null ) {
            RequestMethodPaths rmPaths = rmPathsByRole.get( role );
            if ( rmPaths != null ) {
                return rmPaths;
            }
        }
        return RequestMethodPaths.NOOP;
    }

    @SuppressWarnings("unused")
    public static class RequestMethodPaths {
        private static final String[] EMPTY_PATHS = new String[0];
        private static final RequestMethodPaths NOOP = new RequestMethodPaths();

        private final Map<RequestMethod, Map<String, String>> infosByPathByMethod = new EnumMap<>( RequestMethod.class );

        private RequestMethodPaths() {
        }

        public boolean isEmpty() {
            return infosByPathByMethod.isEmpty();
        }

        public Set<RequestMethod> getRequestMethods() {
            return infosByPathByMethod.keySet();
        }

        public String[] getPaths( RequestMethod method ) {
            if ( method != null ) {
                Map<String, String> infosByPath = infosByPathByMethod.get( method );
                if ( infosByPath != null ) {
                    return infosByPath.keySet().toArray( EMPTY_PATHS ); // if not empty, will create a new array of the appropriate size!
                }
            }
            return EMPTY_PATHS;
        }

        private void add( RequestMethod method, String path, String infos ) {
            infosByPathByMethod.computeIfAbsent( method, key -> new TreeMap<>() ).put( path, infos );
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append( "RequestMethodPaths:" );
            if ( isEmpty() ) {
                sb.append( " empty" );
            } else {
                addToString( sb, "   " );
            }
            return sb.toString();
        }

        private void addToString( StringBuilder sb, String padding ) {
            Set<Map.Entry<RequestMethod, Map<String, String>>> zEntries = infosByPathByMethod.entrySet();
            for ( Map.Entry<RequestMethod, Map<String, String>> pbm : zEntries ) {
                RequestMethod rm = pbm.getKey();
                Map<String, String> infosByPath = pbm.getValue();
                sb.append( '\n' ).append( padding ).append( rm ).append( ':' );
                for ( Map.Entry<String, String> ibp : infosByPath.entrySet() ) {
                    String path = ibp.getKey();
                    String infos = ibp.getValue();
                    sb.append( '\n' ).append( padding ).append( "   " ).append( path );
                    if ( !infos.isEmpty() ) {
                        sb.append( "   (" ).append( infos ).append( ")" );
                    }
                }
            }
        }
    }

    private static Endpoint endpoint;

    private final Map<Role, RequestMethodPaths> rmPathsByRole = new HashMap<>();
    private final Role defaultRole;
    private final List<Role> roles;
    private final int controllerCount;

    private void add( Role role, RequestMethod method, String path, String infos ) { // NOSONAR
        rmPathsByRole.computeIfAbsent( role, key -> new RequestMethodPaths() ).add( method, path, infos );
    }

    private Endpoint( List<Role> roles, List<Controller> controllers ) {
        defaultRole = extractDefaultRole( roles );
        this.roles = roles;
        controllerCount = controllers.size();
        for ( Controller c : controllers ) {
            if ( c != null ) {
                process( c.getClass() );
            }
        }
    }

    private void process( Class<?> c ) {
        try {
            ControllerClass controller = new ControllerClass( c );
            do {
                controller.processMethods( c );
                c = c.getSuperclass();
            } while ( c != null );
        }
        catch ( RuntimeException e ) {
            throw ProcessingException.from( c, e );
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append( "Endpoint:\n   Roles:" );
        for ( Role role : roles ) {
            sb.append( "\n      " ).append( role );
            if ( role == defaultRole ) {
                sb.append( "   (default)" );
            }
            RequestMethodPaths rmPaths = pathsFor( role );
            if ( !rmPaths.isEmpty() ) {
                rmPaths.addToString( sb.append( ':' ), "         " );
            }
        }
        return sb.toString();
    }

    private class ControllerClass {
        private final Role roleFromClass;
        private final String pathFromClass;
        private final boolean deprecatedFromClass;
        private final List<String> updatedFromClass;

        public ControllerClass( Class<?> c ) {
            if ( c.getName().contains( "$$EnhancerBySpringCGLIB" ) ) {
                throw new IllegalStateException( "Spring Enhanced classes (often caused by '@Transactional') are not supported" );
            }
            ClassInterestingAnnotations ca = new ClassInterestingAnnotations();
            ca.process( c.getDeclaredAnnotations() );
            roleFromClass = ca.role;
            pathFromClass = (ca.webPath == null) ? "" : ca.webPath;
            deprecatedFromClass = ca.deprecated;
            updatedFromClass = ca.updated;
        }

        public void processMethods( Class<?> c ) {
            Method[] methods = c.getDeclaredMethods();
            for ( Method method : methods ) {
                if ( method != null ) {
                    processMethods( method );
                }
            }
        }

        private void processMethods( Method method ) {
            MethodInterestingAnnotations ma = new MethodInterestingAnnotations();
            try {
                ma.process( method.getDeclaredAnnotations() );
                addMethodAnnotations( ma );
            }
            catch ( RuntimeException e ) {
                throw new RuntimeException( "on " + method.getName() + ", " + e.getMessage(), e ); // NOSONAR
            }
        }

        private void addMethodAnnotations( MethodInterestingAnnotations ma ) {
            String path = ma.mergePaths( pathFromClass );
            if ( path == null ) {
                return;
            }
            RequestMethod rm = ma.method;
            if ( rm == null ) {
                throw new IllegalStateException( "no RequestMethod provided for path: " + path );
            }
            Role role = ma.mergeRoles( roleFromClass );

            boolean deprecated = ma.mergeDeprecated( deprecatedFromClass );
            List<String> updated = ma.mergeUpdated( updatedFromClass );
            String infos = createInfos( deprecated, updated );

            add( role, rm, normalizeWebPath( path ), infos );
        }
    }

    private abstract class CommonInterestingAnnotations {
        protected final List<String> updated = new ArrayList<>();
        protected boolean deprecated;
        protected String webPath;
        protected Role role = defaultRole;

        public void process( Annotation[] annotations ) {
            if ( (annotations != null) && (annotations.length != 0) ) {
                for ( Annotation annotation : annotations ) {
                    Class<? extends Annotation> annotationClass = annotation.annotationType();
                    try {
                        process( annotationClass, annotation );
                    }
                    catch ( RuntimeException e ) {
                        throw new RuntimeException( "on @" + annotationClass.getSimpleName() + ", " + e.getMessage(), e ); // NOSONAR
                    }
                }
            }
        }

        public boolean mergeDeprecated( boolean deprecatedFromClass ) {
            return this.deprecated || deprecatedFromClass;
        }

        public List<String> mergeUpdated( List<String> updatedFromClass ) {
            if ( updated.isEmpty() ) {
                return updatedFromClass;
            }
            if ( updatedFromClass.isEmpty() ) {
                return updated;
            }
            ArrayList<String> list = new ArrayList<>();
            list.addAll( updatedFromClass );
            list.addAll( updated );
            return list;
        }

        public String mergePaths( String webPathFromClass ) {
            if ( webPath != null ) {
                String fullPath = webPathFromClass + webPath;
                if ( !fullPath.isEmpty() ) {
                    return fullPath;
                }
            }
            return null;
        }

        public Role mergeRoles( Role classRole ) {
            return (role != defaultRole) ? role : classRole;
        }

        protected void process( Class<? extends Annotation> annotationClass, Annotation annotation ) {
            if ( annotationClass == RequestMapping.class ) {
                process( (RequestMapping)annotation );
            } else if ( annotationClass == Updated.class ) {
                process( (Updated)annotation );
            } else if ( annotationClass == Deprecated.class ) {
                process( (Deprecated)annotation );
            } else {
                for ( Role option : roles ) {
                    if ( option.isAccessAnnotationClass( annotationClass ) ) {
                        updateRole( option );
                        return;
                    }
                }
            }
        }

        private void updateRole( Role newRole ) { // newRole should NOT be a User
            if ( role != newRole ) {
                if ( role != defaultRole ) {
                    throw new IllegalStateException( "can't have two non-" + defaultRole + " roles: '" + role + "' and '" + newRole + "'" );
                }
                role = newRole;
            }
        }

        @SuppressWarnings("unused")
        protected void process( Deprecated annotation ) {
            deprecated = true;
        }

        protected void process( Updated annotation ) {
            String date = annotation.date().trim();
            if ( !date.matches( "[0-9]{4}-[0-9]{2}-[0-9]{2}.*" ) ) {
                throw new IllegalStateException( "Updated annotation, but no 'date' specified" );
            }
            while ( true ) {
                // yyyy-mm-dd[, {repeat}]
                // 01234567890
                updated.add( date.substring( 0, 10 ) );
                date = date.substring( 10 ).trim();
                if ( date.isEmpty() ) {
                    return;
                }
                if ( date.startsWith( "," ) ) {
                    date = date.substring( 1 ).trim();
                    if ( date.matches( "[0-9]{4}-[0-9]{2}-[0-9]{2}.*" ) ) {
                        continue;
                    }
                }
                throw new IllegalStateException( "Updated annotation, unexpected value starting with: " + date );
            }
        }

        protected void process( RequestMapping annotation ) {
            path( annotation.value(), annotation.path() );
        }

        protected void path( String[] values, String[] paths ) {
            Set<String> combinedPaths = new HashSet<>();
            appendTo( combinedPaths, values );
            appendTo( combinedPaths, paths );
            switch ( combinedPaths.size() ) {
                case 0:
                    webPath = "";
                    break;
                case 1:
                    webPath = combinedPaths.iterator().next();
                    break;
                default:
                    throw new IllegalStateException(
                            "multiple Web Paths are not supported, but found: " + combinedPaths );
            }
        }
    }

    private class ClassInterestingAnnotations extends CommonInterestingAnnotations {
    }

    private class MethodInterestingAnnotations extends CommonInterestingAnnotations {
        private RequestMethod method;

        @Override
        protected void process( Class<? extends Annotation> annotationClass, Annotation annotation ) {
            if ( annotationClass == GetMapping.class ) {
                process( (GetMapping)annotation );
            } else if ( annotationClass == PostMapping.class ) {
                process( (PostMapping)annotation );
            } else if ( annotationClass == PutMapping.class ) {
                process( (PutMapping)annotation );
            } else if ( annotationClass == PatchMapping.class ) {
                process( (PatchMapping)annotation );
            } else if ( annotationClass == DeleteMapping.class ) {
                process( (DeleteMapping)annotation );
            } else {
                super.process( annotationClass, annotation );
            }
        }

        @Override
        protected void process( RequestMapping annotation ) {
            super.process( annotation );
            RequestMethod[] methods = annotation.method();
            if ( methods.length != 0 ) {
                method = methods[0];
            }
        }

        private void process( GetMapping annotation ) {
            method = RequestMethod.GET;
            path( annotation.value(), annotation.path() );
        }

        private void process( PostMapping annotation ) {
            method = RequestMethod.POST;
            path( annotation.value(), annotation.path() );
        }

        private void process( PutMapping annotation ) {
            method = RequestMethod.PUT;
            path( annotation.value(), annotation.path() );
        }

        private void process( PatchMapping annotation ) {
            method = RequestMethod.PATCH;
            path( annotation.value(), annotation.path() );
        }

        private void process( DeleteMapping annotation ) {
            method = RequestMethod.DELETE;
            path( annotation.value(), annotation.path() );
        }
    }

    private static void appendTo( Set<String> collector, String[] values ) { // NOSONAR
        if ( values != null ) {
            for ( String value : values ) {
                if ( (value != null) && !value.isEmpty() ) { // Left to Right
                    collector.add( value );
                }
            }
        }
    }

    private static String createInfos( boolean deprecated, List<String> updated ) {
        String rv = deprecated ? "** DEPRECATED **" : "";
        if ( updated.isEmpty() ) {
            return rv;
        }
        StringBuilder sb = new StringBuilder( rv );
        if ( deprecated ) {
            sb.append( ", " );
        }
        sb.append( "Updated: " );
        if ( updated.size() == 1 ) {
            sb.append( updated.get( 0 ) );
        } else {
            Collections.sort( updated );
            sb.append( updated.get( 0 ) );
            for ( int i = 1; i < updated.size(); i++ ) {
                sb.append( ", " ).append( updated.get( i ) );
            }
        }
        return sb.toString();
    }

    private static Role extractDefaultRole( List<Role> roles ) {
        Role defaultRole = null;
        for ( Role role : roles ) {
            if ( role == null ) {
                throw new IllegalArgumentException( "Null Role?" );
            }
            if ( role.isDefaultRole() ) {
                if ( defaultRole != null ) {
                    throw new IllegalArgumentException( "More than one 'default' Role?" );
                }
                defaultRole = role;
            }
        }
        if ( defaultRole == null ) {
            throw new IllegalArgumentException( "No 'default' Role?" );
        }
        return defaultRole;
    }

    private static String normalizeWebPath( String path ) { // NOSONAR
        int closeAt = path.indexOf( '}' );
        if ( closeAt == -1 ) {
            return path;
        }
        StringBuilder sb = new StringBuilder( path );
        for ( int openAt; -1 != (openAt = sb.lastIndexOf( "{", closeAt )); ) {
            sb.delete( openAt, closeAt );
            sb.setCharAt( openAt, '*' );
            closeAt = sb.indexOf( "}", openAt );
            if ( closeAt == -1 ) {
                break;
            }
        }
        return sb.toString();
    }
}
