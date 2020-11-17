package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskCommandStar extends TaskCommand {
    public TaskCommandStar( String id ) {
        super( id );
    }
}
