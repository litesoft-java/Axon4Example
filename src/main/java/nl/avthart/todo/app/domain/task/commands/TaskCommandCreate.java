package nl.avthart.todo.app.domain.task.commands;

import javax.validation.constraints.NotNull;

import lombok.Builder;
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

    @Builder
    public TaskCommandCreate( String id, String username, String title ) {
        super( id );
        this.username = username;
        this.title = title;
    }
}
