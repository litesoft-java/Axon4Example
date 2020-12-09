package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractTaskCommandLoad extends AbstractAllFieldsTaskCommand {
    protected AbstractTaskCommandLoad( String id,
                                       String createdHour,
                                       String username,
                                       String title,
                                       boolean completed,
                                       boolean starred ) {
        super( id, createdHour, username, title, completed, starred );
    }
}
