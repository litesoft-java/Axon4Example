package nl.avthart.todo.app.domain.task.commands;

import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class TaskCommandModifyTitle extends TaskCommand {
    @NotNull
    private final String title;

    public TaskCommandModifyTitle( String id, String pTitle ) {
        super( id );
        title = pTitle;
    }
}

