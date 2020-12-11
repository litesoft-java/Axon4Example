package nl.avthart.todo.app.domain.task;

import java.util.HashMap;
import java.util.Map;

import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class TaskLoadableTest {
    private static final TaskLoadable LOADABLE = new TaskLoadable();

    @Test
    void createCommand() {
        check( LOADABLE.createCommand( populate( "title", "My Task"
        ) ), TaskCommandLoad.builder()
                       .username( TaskLoadable.LOADED )
                       .title( "My Task" )
                       .starred( false )
                       .completed( false )
                       .build() );

        check( LOADABLE.createCommand( populate( "username", "Fred",
                                                 "title", "My-Task",
                                                 "createdHour", "2011-01-16T12Z",
                                                 "starred", true,
                                                 "completed", true

        ) ), TaskCommandLoad.builder()
                       .username( "Fred" )
                       .title( "My-Task" )
                       .createdHour( "2011-01-16T12Z" )
                       .starred( true )
                       .completed( true )
                       .build() );

        checkBoolean( false, "n", "N", "no", "NO", "f", "F", "false", "FALSE" );
        checkBoolean( true, "y", "Y", "yes", "YES", "t", "T", "true", "TRUE" );

        try {
            TaskCommandLoad command = LOADABLE.createCommand( populate( "title", "My-Task", "createdHour", "2011-01-16t12Z" ) );
            fail("Expected command construction to fail: " + command);
        }
        catch ( IllegalArgumentException e ) {
            if (!e.getMessage().contains( "did not match" )) {
                throw e;
            }
        }

        try {
            TaskCommandLoad command = LOADABLE.createCommand( populate( "title", "" ) ); // No Title
            fail("Expected command construction to fail: " + command);
        }
        catch ( IllegalArgumentException e ) {
            if (!e.getMessage().contains( "required String" )) {
                throw e;
            }
        }

        try {
            TaskCommandLoad command = LOADABLE.createCommand( populate() ); // No Title
            fail("Expected command construction to fail: " + command);
        }
        catch ( IllegalArgumentException e ) {
            if (!e.getMessage().contains( "required String" )) {
                throw e;
            }
        }
    }

    private void checkBoolean( boolean expectedBoolean, String... textOptions ) {
        TaskCommandLoad expected = TaskCommandLoad.builder()
                .username( TaskLoadable.LOADED )
                .title( "My Task" )
                .starred( expectedBoolean )
                .completed( expectedBoolean )
                .build();
        check( LOADABLE.createCommand( populate( "title", "My Task",
                                                 "starred", expectedBoolean,
                                                 "completed", expectedBoolean
        ) ), expected );
        for ( String option : textOptions ) {
            check( LOADABLE.createCommand( populate( "title", "My Task",
                                                     "starred", option,
                                                     "completed", option
            ) ), expected );
        }
    }

    private void check( TaskCommandLoad actual, TaskCommandLoad expected ) {
        assertEquals( expected, actual );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, ?> populate( Object... keyValues ) {
        Map map = new HashMap<>();
        if ( keyValues != null ) {
            if ( (keyValues.length & 1) == 1 ) { // odd number of keyValues?
                throw new IllegalArgumentException( "Missing Value for key: " + keyValues[keyValues.length - 1] );
            }
            for ( int i = 0; i < keyValues.length; i += 2 ) {
                Object key = keyValues[i];
                if ( !(key instanceof String) ) {
                    throw new IllegalArgumentException( "keyValues[" + i + "] not a string (" +
                                                        ((key == null) ? "null" : key.getClass().getSimpleName()) + "): " + key );
                }
                Object value = keyValues[i + 1];
                if ( value != null ) {
                    map.put( key, value );
                }
            }
        }
        return (Map<String, ?>)map;
    }
}