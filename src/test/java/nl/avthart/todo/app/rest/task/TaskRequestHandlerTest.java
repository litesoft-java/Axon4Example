package nl.avthart.todo.app.rest.task;

import nl.avthart.todo.app.AbstractTaskTestSupport;
import nl.avthart.todo.app.query.task.TaskEntity;
import nl.avthart.todo.app.rest.task.requests.TaskRequestCreate;
import nl.avthart.todo.app.rest.task.requests.TaskRequestModifyTitle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskRequestHandlerTest extends AbstractTaskTestSupport {
    private static final String USER = "Fred";

    @Autowired
    private TaskRequestHandler handler;

    @Test
    public void scenarios() {
        String title = "Lunch";
        String id = handler.create( USER, TaskRequestCreate.builder()
                .title( title )
                .build() );
        String taskId = checkTask( get( handler, USER, false, 1, id ),
                                   0, false, title, false ).getId();

        handler.star( taskId );
        checkTask( get( handler, USER, false, 1, id ),
                   1, true, title, false );

        title += " Meeting";
        handler.modifyTitle( taskId, TaskRequestModifyTitle.builder()
                .title( title )
                .build() );
        checkTask( get( handler, USER, false, 1, id ),
                   2, true, title, false );

        handler.delete( taskId );
        get( handler, USER, false, 0, id );

        handler.restore( taskId );
        checkTask( get( handler, USER, false, 1, id ),
                   4, true, title, false );

        handler.complete( taskId );
        checkTask( get( handler, USER, true, 1, id ),
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
}
