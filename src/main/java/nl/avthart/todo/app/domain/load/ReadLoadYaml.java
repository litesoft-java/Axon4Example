package nl.avthart.todo.app.domain.load;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

@Component
public class ReadLoadYaml {
    public static class ParsingException extends RuntimeException {
        public ParsingException( String message ) {
            super( message );
        }

        public ParsingException( String message, Throwable cause ) {
            super( message, cause );
        }
    }

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper( new YAMLFactory() );
    private static final TypeReference<HashMap<String, ?>> TYPE_REFERENCE = new TypeReference<HashMap<String, ?>>() {
    };

    public Map<String, List<Map<String, ?>>> read( String source, String... lines ) {
        String content = "";
        if ( (lines != null) && (lines.length > 0) ) {
            content = String.join( "\n", lines ) + "\n";
        }
        Map<String, ?> map;
        try {
            map = YAML_MAPPER.readValue( content, TYPE_REFERENCE );
        }
        catch ( JsonProcessingException e ) {
            throw new ParsingException( "From '" + source + "': " + e.getMessage(), e );
        }
        String exceptionPrefix = "From '" + source + "': Value for '";
        for ( Map.Entry<String, ?> entry : map.entrySet() ) {
            String entryExceptionPrefix = exceptionPrefix + entry.getKey();
            Object value = entry.getValue();
            if ( !(value instanceof List) ) {
                throw new ParsingException( entryExceptionPrefix + notA( "list", value ) );
            }
            List<?> list = (List<?>)value;
            for ( int i = 0; i < list.size(); i++ ) {
                String listEntryExceptionPrefix = entryExceptionPrefix + "[" + i + "]";
                Object o = list.get( i );
                if ( !(o instanceof Map) ) {
                    throw new ParsingException( listEntryExceptionPrefix + notA( "map", o ) );
                }
                Map<?, ?> listEntry = (Map<?, ?>)o;
                for ( Object key : listEntry.keySet() ) {
                    String keyEntryExceptionPrefix = listEntryExceptionPrefix + "." + key;
                    if ( !(key instanceof String) ) {
                        throw new ParsingException( keyEntryExceptionPrefix + notA( "string", key ) );
                    }
                    String sKey = key.toString();
                    if ( sKey.isEmpty() ) {
                        throw new ParsingException( keyEntryExceptionPrefix + "' was empty." );
                    }
                    int c = sKey.codePointAt( 0 );
                    if ( (c != '_') && !Character.isAlphabetic( c ) ) {
                        throw new ParsingException( keyEntryExceptionPrefix + "', expected key to start with a letter or underscore (_), it was: " + sKey );
                    }
                    if ( sKey.contains(" ") ) {
                        throw new ParsingException( keyEntryExceptionPrefix + "', expected key may not contains a space, it was: " + sKey );
                    }
                }
            }
        }
        //noinspection unchecked
        return (Map<String, List<Map<String, ?>>>)map;
    }

    private static String notA( String what, Object value ) {
        return "' not a " + what + ", but was (" + value.getClass().getSimpleName() + "): " + value;
    }
}
