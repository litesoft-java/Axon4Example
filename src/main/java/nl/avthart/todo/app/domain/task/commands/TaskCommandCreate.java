package nl.avthart.todo.app.domain.task.commands;

import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class TaskCommandCreate extends TaskCommand {
    @NotNull
    private final String username;

    @NotNull
    private final String title;

    public TaskCommandCreate( String id, String pUsername, String pTitle ) {
        super( id );
        username = pUsername;
        title = pTitle;
    }
}
