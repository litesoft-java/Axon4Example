package nl.avthart.todo.app.domain.task;

import nl.avthart.todo.app.AbstractTaskTestSupport;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class TaskLoadableTest extends AbstractTaskTestSupport {
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
            fail( "Expected command construction to fail: " + command );
        }
        catch ( IllegalArgumentException e ) {
            if ( !e.getMessage().contains( "did not match" ) ) {
                throw e;
            }
        }

        try {
            TaskCommandLoad command = LOADABLE.createCommand( populate( "title", "" ) ); // No Title
            fail( "Expected command construction to fail: " + command );
        }
        catch ( IllegalArgumentException e ) {
            if ( !e.getMessage().contains( "required String" ) ) {
                throw e;
            }
        }

        try {
            TaskCommandLoad command = LOADABLE.createCommand( populate() ); // No Title
            fail( "Expected command construction to fail: " + command );
        }
        catch ( IllegalArgumentException e ) {
            if ( !e.getMessage().contains( "required String" ) ) {
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
}