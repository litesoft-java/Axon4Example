package nl.avthart.todo.app.domain.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Component;

@Component
public class Loader {
    @SuppressWarnings("unused")
    public static class SuccessPair {
        private final String id, what;

        private SuccessPair( Object id, String what ) {
            this.id = (id == null) ? null : id.toString();
            this.what = what;
        }

        public String getId() {
            return id;
        }

        public String getWhat() {
            return what;
        }

        @Override
        public String toString() {
            return id + " <= " + what;
        }
    }

    public static class Result {
        private final List<String> errors = new ArrayList<>();
        private final List<SuccessPair> loaded = new ArrayList<>();

        private void addError( String error ) {
            errors.add( error );
        }

        private void addSuccess( Object id, String what ) {
            loaded.add( new SuccessPair( id, what ) );
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if ( !errors.isEmpty() ) {
                sb.append( "Errors:\n" );
                for ( String error : errors ) {
                    sb.append( "   " ).append( error );
                }
            }
            if ( !loaded.isEmpty() ) {
                sb.append( "loaded:\n" );
                for ( SuccessPair pair : loaded ) {
                    sb.append( "   " ).append( pair );
                }
            }
            return sb.toString();
        }
    }

    private final CommandGateway commandGateway;
    private final Map<String, Loadable<?>> loadablesByType = new HashMap<>();

    public Loader( CommandGateway commandGateway, List<Loadable<?>> loadables ) {
        this.commandGateway = commandGateway;
        for ( Loadable<?> loadable : loadables ) {
            loadablesByType.put( loadable.type(), loadable );
        }
    }

    /**
     * Load rows of the typesByType
     *
     * @param typesByType result of ReadLoadYaml
     * @return list of errors
     */
    public Result load( Map<String, List<Map<String, ?>>> typesByType ) {
        Result result = new Result();
        for ( Map.Entry<String, List<Map<String, ?>>> entry : typesByType.entrySet() ) {
            String key = entry.getKey();
            Loadable<?> loadable = loadablesByType.get( key );
            if ( loadable == null ) {
                result.addError( "Unrecognized type '" + key + "'" );
                continue;
            }
            for ( Map<String, ?> map : entry.getValue() ) {
                try {
                    LoadCommand command = loadable.createCommand( map );
                    Object id = commandGateway.sendAndWait( command );
                    result.addSuccess( id, map.toString() );
                }
                catch ( RuntimeException e ) {
                    result.addError( "Problem type '" + key + "' - " + map + " - " + e.getMessage() );
                }
            }
        }
        return result;
    }
}
