package nl.avthart.todo.app.rest.task;

import nl.avthart.todo.app.query.task.TaskActive;
import nl.avthart.todo.app.rest.task.requests.TaskRequestCreate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class TaskRequestHandlerTest {
    private static final String USER = "Fred";

    @Autowired
    private TaskRequestHandler handler;

    @Test
    public void scenarios() {
        handler.create( USER, TaskRequestCreate.builder()
                .title( "Lunch" )
                .build() );
        Page<TaskActive> page = handler.findAll( USER, false, null );
        System.out.println(page);
    }
}