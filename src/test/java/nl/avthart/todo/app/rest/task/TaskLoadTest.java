package nl.avthart.todo.app.rest.task;

import nl.avthart.todo.app.AbstractTaskTestSupport;
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
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskLoadTest extends AbstractTaskTestSupport {
    private static final String USER = "Fred";

    private final IdentifierFactory identifierFactory = IdentifierFactory.getInstance();

    @Autowired
    private TaskRequestHandler handler;

    @Autowired
    private CommandGateway commandGateway;

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

        TaskActive task = get( handler, USER, false, 1, loadCommand.getId() );

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

        task = get( handler, USER, true, 1, loadCommand.getId() );

        checkExpected( overwrite, task );

        // Optional Overwrite Load - create
        overwrite = TaskCommandLoadOverwrite.builder()
                .id( identifierFactory.generateIdentifier() )
                .username( USER )
                .title( title + "-3" )
                .build();

        rv = commandGateway.sendAndWait( overwrite );

        assertEquals( overwrite.getId(), rv );

        task = get( handler, USER, false, 1, overwrite.getId() );

        checkExpected( overwrite, task );

        // Optional Load - first time -> create - No ID
        loadCommand = TaskCommandLoad.builder()
                .username( USER )
                .title( title + "-4" )
                .build();

        rv = commandGateway.sendAndWait( loadCommand );

        assertNotNull( rv ); // ID

        task = get( handler, USER, false, 2, rv.toString() );

        loadCommand.setId( rv.toString() ); // ID

        checkExpected( loadCommand, task );
    }

    private void checkExpected( AbstractTaskCommandLoad command, TaskActive task ) {
        assertEquals( command.getId(), task.getId() );

        if ( command.getCreatedHour() == null ) {
            command.setCreatedHour( task.getCreatedHour() );
        }

        if ( !command.isEquivalent( task ) ) {
            fail( command.delta( task ) );
        }
    }
}
