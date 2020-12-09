package nl.avthart.todo.app.rest.task;

import java.util.Arrays;
import java.util.List;

import nl.avthart.todo.app.common.exceptions.CantUpdateException;
import nl.avthart.todo.app.domain.task.commands.AbstractTaskCommandLoad;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoadOverwrite;
import nl.avthart.todo.app.query.task.TaskActive;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.IdentifierFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskLoadTest {
    private static final String USER = "Fred";

    private final IdentifierFactory identifierFactory = IdentifierFactory.getInstance();

    @Autowired
    private TaskRequestHandler handler;

    @Autowired
    private CommandGateway commandGateway;

    @SuppressWarnings("ConstantConditions")
    @Test
    public void scenarios() {
        String title = "Meeting";

        // Optional Load - first time -> create
        TaskCommandLoad loadCommand = TaskCommandLoad.builder()
                .id( identifierFactory.generateIdentifier() )
                .username( USER )
                .title( title )
                .build();

        Object rv = commandGateway.sendAndWait( loadCommand );

        assertEquals( loadCommand.getId(), rv );

        TaskActive task = get( false, 1 );

        checkExpected( loadCommand, task );

        // Optional Load already there
        rv = commandGateway.sendAndWait( loadCommand );

        assertNull( rv );

        // Optional Overwrite Load already there
        TaskCommandLoadOverwrite overwrite = TaskCommandLoadOverwrite.builder()
                .id( loadCommand.getId() )
                .username( loadCommand.getUsername() )
                .title( loadCommand.getTitle() )
                .starred( loadCommand.isStarred() )
                .completed( loadCommand.isCompleted() )
                .build();

        rv = commandGateway.sendAndWait( overwrite );

        assertNull( rv );

        // Optional Load w/o Overwrite but different!
        loadCommand.setTitle( title + "-2" );

        try {
            rv = commandGateway.sendAndWait( loadCommand );
            fail( "Non-overwrite, but succeeded with rv: " + rv );
        }
        catch ( CantUpdateException expected ) {
            // Ignore
        }

        // Optional Overwrite Load - with replace preexisting load
        overwrite = TaskCommandLoadOverwrite.builder()
                .id( task.getId() )
                .username( USER )
                .title( title + "-2" )
                .starred( true )
                .completed( true )
                .build();

        rv = commandGateway.sendAndWait( overwrite );

        assertEquals( overwrite.getId(), rv );

        task = get( true, 1 );

        checkExpected( overwrite, task );

        // Optional Overwrite Load - create
        overwrite = TaskCommandLoadOverwrite.builder()
                .id( identifierFactory.generateIdentifier() )
                .username( USER )
                .title( title + "-3" )
                .build();

        rv = commandGateway.sendAndWait( overwrite );

        assertEquals( overwrite.getId(), rv );

        task = get( false, 1 );

        checkExpected( overwrite, task );

        // Optional Load - first time -> create - No ID
        loadCommand = TaskCommandLoad.builder()
                .username( USER )
                .title( title + "-4" )
                .build();

        rv = commandGateway.sendAndWait( loadCommand );

        assertNotNull( rv ); // ID

        task = get( false, 2, overwrite.getId() );

        loadCommand.setId(rv.toString()); // ID

        checkExpected( loadCommand, task );
    }

    private void checkExpected( AbstractTaskCommandLoad command, TaskActive task ) {
        assertEquals( command.getId(), task.getId() );

        if (command.getCreatedHour() == null) {
            command.setCreatedHour( task.getCreatedHour() );
        }

        if ( !command.isEquivalent( task ) ) {
            fail( command.delta( task ) );
        }
    }

    private TaskActive get( boolean completed, int expected, String... skipIds ) {
        Page<TaskActive> page = handler.findAll( USER, completed, null );
        assertNotNull( page );
        List<TaskActive> tasks = page.getContent();
        assertNotNull( tasks );
        assertEquals( expected, tasks.size() );
        if ( expected != 0 ) {
            if ( skipIds.length == 0 ) {
                return tasks.get( 0 );
            }
            List<String> skipIdCollection = Arrays.asList( skipIds );
            for ( TaskActive task : tasks ) {
                if ( !skipIdCollection.contains( task.getId() ) ) {
                    return task;
                }
            }
            return tasks.get( 0 );
        }
        return null;
    }
}
