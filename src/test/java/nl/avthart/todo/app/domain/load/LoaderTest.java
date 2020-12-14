package nl.avthart.todo.app.domain.load;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.avthart.todo.app.AbstractTaskTestSupport;
import nl.avthart.todo.app.common.exceptions.BusinessRuleException;
import nl.avthart.todo.app.common.exceptions.CantUpdateException;
import nl.avthart.todo.app.domain.task.TaskLoadable;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.IdentifierFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class LoaderTest extends AbstractTaskTestSupport {

    @Mock
    CommandGateway commandGateway;

    @Test
    void load() {
        commandGateway = Mockito.mock( CommandGateway.class );

        Loader loader = new Loader( commandGateway, Collections.singletonList( new TaskLoadable() ) );

        String id1 = IdentifierFactory.getInstance().generateIdentifier();

        Map<String, ?> task1 = populate( "id", id1,
                                         "title", "My Task" );
        TaskCommandLoad cmd1 = TaskCommandLoad.builder()
                .id( id1 )
                .username( TaskLoadable.LOADED )
                .title( "My Task" )
                .starred( false )
                .completed( false )
                .build();

        String id2 = IdentifierFactory.getInstance().generateIdentifier();

        Map<String, ?> task2 = populate( "id", id2,
                                         "title", "My Other Task" );
        TaskCommandLoad cmd2 = TaskCommandLoad.builder()
                .id( id2 )
                .username( TaskLoadable.LOADED )
                .title( "My Other Task" )
                .starred( false )
                .completed( false )
                .build();

        when( commandGateway.sendAndWait( any( TaskCommandLoad.class ) ) ).thenAnswer(
                (Answer<String>)invocation -> {
                    TaskCommandLoad tcl = invocation.getArgument( 0, TaskCommandLoad.class );
                    if ( cmd1.equals( tcl ) ) {
                        return cmd1.getId();
                    }
                    if ( cmd2.equals( tcl ) ) {
                        throw new CantUpdateException( "Task " + cmd2.getId() );
                    }
                    throw new BusinessRuleException( "Unexpected: " + tcl );
                }
        );

        Loader.Result result = loader.load( create( task1, task2 ) );

        try {
            assertEquals( 1, result.getErrors().size() );
            assertEquals( 1, result.getLoaded().size() );
            assertEquals( id1, result.getLoaded().get( 0 ).getId() );
            assertEquals( "Problem type 'Task' - " + task2 + " - (CantUpdateException): Task " + id2,
                          result.getErrors().get( 0 ) );
        }
        catch ( RuntimeException | Error e ) {
            System.out.println( "LoaderTest.loadSingle: " + result );
            throw e;
        }
    }

    private Map<String, List<Map<String, ?>>> create( Map<String, ?>... maps ) {
        List<Map<String, ?>> list = Arrays.asList( maps );
        Map<String, List<Map<String, ?>>> map = new HashMap<>();
        map.put( "Task", list );
        return map;
    }
}