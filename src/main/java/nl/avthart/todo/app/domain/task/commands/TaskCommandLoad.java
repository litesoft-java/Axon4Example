package nl.avthart.todo.app.domain.task.commands;

import lombok.Builder;
import lombok.ToString;

@ToString(callSuper = true)
public class TaskCommandLoad extends AbstractTaskCommandLoad {

    @Builder(toBuilder = true)
    public TaskCommandLoad( String id,
                            String createdHour,
                            String username,
                            String title,
                            boolean completed,
                            boolean starred ) {
        super( id, createdHour, username, title, completed, starred );
    }
}
