package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskCommandComplete extends TaskCommand {
    public TaskCommandComplete( String id ) {
        super( id );
    }
}