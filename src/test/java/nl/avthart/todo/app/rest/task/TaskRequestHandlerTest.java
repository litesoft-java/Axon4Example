package nl.avthart.todo.app.rest.task;

import java.util.List;

import nl.avthart.todo.app.query.task.TaskActive;
import nl.avthart.todo.app.query.task.TaskEntity;
import nl.avthart.todo.app.rest.task.requests.TaskRequestCreate;
import nl.avthart.todo.app.rest.task.requests.TaskRequestModifyTitle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskRequestHandlerTest {
    private static final String USER = "Fred";

    @Autowired
    private TaskRequestHandler handler;

    @Test
    public void scenarios() {
        String title = "Lunch";
        handler.create( USER, TaskRequestCreate.builder()
                .title( title )
                .build() );
        String taskId = checkTask( get( false, 1 ),
                                   0, false, title, false ).getId();

        handler.star( taskId );
        checkTask( get( false, 1 ),
                   1, true, title, false );

        title += " Meeting";
        handler.modifyTitle( taskId, TaskRequestModifyTitle.builder()
                .title( title )
                .build() );
        checkTask( get( false, 1 ),
                   2, true, title, false );

        handler.delete( taskId );
        get( false, 0 );

        handler.restore( taskId );
        checkTask( get( false, 1 ),
                   4, true, title, false );

        handler.complete( taskId );
        checkTask( get( true, 1 ),
                   5, true, title, true );
    }

    private <T extends TaskEntity> T checkTask( T task,
                                                long version, boolean starred, String title, boolean completed ) {
        assertNotNull( task );

        try {
            assertEquals( new Long( version ), task.getVersion() );
            assertEquals( starred, task.isStarred() );
            assertEquals( title, task.getTitle() );
            assertEquals( completed, task.isCompleted() );
            assertEquals( USER, task.getUsername() );
        }
        catch ( RuntimeException e ) {
            System.out.println( task );
            throw e;
        }
        return task;
    }

    private TaskActive get( boolean completed, int expected ) {
        Page<TaskActive> page = handler.findAll( USER, completed, null );
        assertNotNull( page );
        List<TaskActive> tasks = page.getContent();
        assertNotNull( tasks );
        assertEquals( expected, tasks.size() );
        return (expected == 0) ? null : tasks.get( 0 );
    }
}
