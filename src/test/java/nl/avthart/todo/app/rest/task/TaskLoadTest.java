package nl.avthart.todo.app.rest.task;

import java.util.Arrays;
import java.util.List;

import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import nl.avthart.todo.app.query.task.TaskActive;
import nl.avthart.todo.app.rest.task.requests.TaskRequestCreate;
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

        TaskCommandLoad loadCommand = TaskCommandLoad.builder()
                .id( identifierFactory.generateIdentifier() )
                .username( USER )
                .title( title )
                .build();

        Object rv = commandGateway.sendAndWait( loadCommand );

        TaskActive task = get( 1 );

        handler.create( USER, TaskRequestCreate.builder()
                .title( title + "-2" )
                .build() );

        TaskActive task2 = get( 2, task.getId() );

        System.out.println( "************ " + rv + ":\n" + task + "\n" + task2 );

        rv = commandGateway.sendAndWait( loadCommand );

        assertNull( rv );

        //    handler.star( taskId );
        //    checkTask( get( false, 1 ),
        //               1, true, title, false );
        //
        //    title += " Meeting";
        //    handler.modifyTitle( taskId, TaskRequestModifyTitle.builder()
        //            .title( title )
        //            .build() );
        //    checkTask( get( false, 1 ),
        //               2, true, title, false );
        //
        //    handler.delete( taskId );
        //    get( false, 0 );
        //
        //    handler.restore( taskId );
        //    checkTask( get( false, 1 ),
        //               4, true, title, false );
        //
        //    handler.complete( taskId );
        //    checkTask( get( true, 1 ),
        //               5, true, title, true );
    }

    private TaskActive get( int expected, String... skipIds ) {
        Page<TaskActive> page = handler.findAll( USER, false, null );
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
                if (!skipIdCollection.contains( task.getId() )) {
                    return task;
                }
            }
            return tasks.get( 0 );
        }
        return null;
    }
}
