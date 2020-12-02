package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskCommandRestore extends TaskCommand {
    public TaskCommandRestore( String id ) {
        super( id );
    }
}
