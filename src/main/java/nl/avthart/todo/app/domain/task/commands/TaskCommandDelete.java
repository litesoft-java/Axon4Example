package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskCommandDelete extends TaskCommand {
    public TaskCommandDelete( String id ) {
        super( id );
    }
}
