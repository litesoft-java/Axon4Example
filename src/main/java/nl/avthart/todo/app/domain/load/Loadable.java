package nl.avthart.todo.app.domain.load;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public interface Loadable<T extends LoadCommand> {
    static String defaultString( String value, String defaultString ) {
        return (value != null) ? value : defaultString;
    }

    static String optionalRegex( String value, String key, String regex ) {
        if ( value == null ) {
            return null;
        }
        if ( !value.matches( regex ) ) {
            throw new IllegalArgumentException( "String '" + key + "' did not match '" + regex + "', value was: " + value );
        }
        return value;
    }

    static String requiredRegex( String value, String key, String regex ) {
        return optionalRegex( requiredString( value, key ), key, regex );
    }

    static String requiredString( String value, String key ) {
        if ( value == null ) {
            throw new IllegalArgumentException( "required String '" + key + "' but none supplied" );
        }
        return value;
    }

    static String requiredMinLengthString( String value, String key, int minLength ) {
        int length = requiredString( value, key ).length();
        if ( length < minLength ) {
            throw new IllegalArgumentException( "required String '" + key + "' too short, must be at least " + minLength + " characters" );
        }
        return value;
    }

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

    static UUID asUUID( Object value, String key ) {
        String str = asString( value, key );
        return (str == null) ? null : UUID.fromString( str );
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
