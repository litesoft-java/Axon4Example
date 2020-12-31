package nl.avthart.todo.app.domain.load;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoadableTest {

    @Test
    void defaultString() {
        assertEquals( "USA", Loadable.defaultString( "USA", "X" ) );
        assertEquals( "X", Loadable.defaultString( null, "X" ) );
    }

    @Test
    void optionalRegex() {
        assertEquals( "USA", Loadable.optionalRegex( "USA", "X", "^[A-Z]{3}$" ) );

        assertNull( Loadable.optionalRegex( null, "X", "^[A-Z]{3}$" ) );

        try {
            String actual = Loadable.optionalRegex( "US", "X", "^[A-Z]{3}$" );
            fail( "Did not expect: " + actual );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            assertTrue( msg.contains( "'X'" ) && msg.contains( "not match" ) );
        }
    }

    @Test
    void requiredRegex() {
        assertEquals( "USA", Loadable.requiredRegex( "USA", "X", "^[A-Z]{3}$" ) );

        try {
            String actual = Loadable.requiredRegex( null, "X", "^[A-Z]{3}$" );
            fail( "Did not expect: " + actual );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            assertTrue( msg.contains( "'X'" ) && msg.contains( "none supplied" ) );
        }

        try {
            String actual = Loadable.requiredRegex( "US", "X", "^[A-Z]{3}$" );
            fail( "Did not expect: " + actual );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            assertTrue( msg.contains( "'X'" ) && msg.contains( "not match" ) );
        }
    }

    @Test
    @SuppressWarnings("ObviousNullCheck")
    void requiredString() {
        assertEquals( "USA", Loadable.requiredString( "USA", "X" ) );

        try {
            String actual = Loadable.requiredString( null, "X" );
            fail( "Did not expect: " + actual );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            assertTrue( msg.contains( "'X'" ) && msg.contains( "none supplied" ) );
        }
    }

    @Test
    void requiredMinLengthString() {
        assertEquals( "USA", Loadable.requiredMinLengthString( "USA", "X", 2 ) );

        try {
            String actual = Loadable.requiredMinLengthString( null, "X", 2 );
            fail( "Did not expect: " + actual );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            assertTrue( msg.contains( "'X'" ) && msg.contains( "none supplied" ) );
        }

        try {
            String actual = Loadable.requiredMinLengthString( "A", "X", 2 );
            fail( "Did not expect: " + actual );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            assertTrue( msg.contains( "'X'" ) && msg.contains( "too short" ) );
        }
    }

    @Test
    void asString() {
        assertNull( Loadable.asString( null, "X" ) );
        assertNull( Loadable.asString( "  ", "X" ) );
        assertEquals( "USA", Loadable.asString( " USA ", "X" ) );
    }

    @Test
    void asUUID() {
        assertNull( Loadable.asUUID( null, "X" ) );
        assertNull( Loadable.asUUID( "  ", "X" ) );
        UUID uuid = UUID.randomUUID();
        assertEquals( uuid, Loadable.asUUID( uuid.toString(), "X" ) );
    }

    @Test
    void asBoolean() {
        assertNull( Loadable.asBoolean( null, "X" ) );
        assertEquals( true, Loadable.asBoolean( true, "X" ) );
        assertEquals( false, Loadable.asBoolean( false, "X" ) );

        checkBoolean( true, "true", "yes" );
        checkBoolean( false, "false", "no" );

        try {
            Boolean actual = Loadable.asBoolean( "Fred", "X" );
            fail( "Did not expect: " + actual );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            assertTrue( msg.contains( "'X'" ) && msg.contains( "expected Boolean" ) );
        }
    }

    private void checkBoolean( boolean expected, String... lowercaseValues ) {
        for ( String value : lowercaseValues ) {
            checkBoolean( value, expected );
            checkBoolean( value.toUpperCase(), expected );
            value = value.substring( 0, 1 );
            checkBoolean( value, expected );
            checkBoolean( value.toUpperCase(), expected );
        }
    }

    private void checkBoolean( String value, boolean expected ) {
        assertEquals( expected, Loadable.asBoolean( value, "X" ) );
    }
}