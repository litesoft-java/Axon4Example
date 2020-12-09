package nl.avthart.todo.app.domain.task.commands;

import lombok.Builder;
import lombok.ToString;
import nl.avthart.todo.app.query.task.AbstractTaskEntry_v001;

@ToString(callSuper = true)
public class TaskCommandUpdate extends AbstractAllFieldsTaskCommand {
    @Builder
    public TaskCommandUpdate( String id,
                              String createdHour,
                              String username,
                              String title,
                              boolean completed,
                              boolean starred ) {
        super( id, createdHour, username, title, completed, starred );
    }

    public TaskCommandUpdate( String id, AbstractTaskEntry_v001 fields ) {
        super( id, fields );
    }
}
