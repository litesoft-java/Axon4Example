package nl.avthart.todo.app.domain.load;

import java.util.Map;

public interface Loadable<T extends LoadCommand> {
    static String optionalRegex( String value, String key, String regex ) {
        if ( value == null ) {
            return null;
        }
        if ( !value.matches( regex ) ) {
            throw new IllegalArgumentException( "String '" + key + "' did not match '" + regex + "', value was: " + value );
        }
        return value;
    }

    static String defaultString( String value, String defaultString ) {
        return (value != null) ? value : defaultString;
    }

    static String requiredString( String value, String key ) {
        if ( value == null ) {
            throw new IllegalArgumentException( "required String '" + key + "' but none supplied" );
        }
        return value;
    }

    @SuppressWarnings("unused")
    static String asString( Object value, String key ) {
        if ( value == null ) {
            return null;
        }
        String str = value.toString();
        if ( str != null ) {
            str = str.trim();
            if ( str.isEmpty() ) {
                str = null;
            }
        }
        return str;
    }

    static Boolean asBoolean( Object value, String key ) {
        if ( value == null ) {
            return null;
        }
        if ( value instanceof Boolean ) {
            return (Boolean)value;
        }
        String str = "|" + value.toString().toUpperCase() + "|";
        if ( "|T|TRUE|Y|YES|".contains( str ) ) {
            return true;
        }
        if ( "|F|FALSE|N|NO|".contains( str ) ) {
            return false;
        }
        throw new IllegalArgumentException( "expected Boolean '" + key + "' but got: " + value );
    }

    String type();

    T createCommand( Map<String, ?> map );
}
