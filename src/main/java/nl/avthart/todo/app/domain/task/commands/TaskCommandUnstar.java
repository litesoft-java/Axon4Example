package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskCommandUnstar extends TaskCommand {
    public TaskCommandUnstar( String id ) {
        super( id );
    }
}