package nl.avthart.todo.app.domain.task.commands;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class TaskCommandModifyTitle extends TaskCommand {
    @NotNull
    private final String title;

    @Builder
    public TaskCommandModifyTitle( String id, String title ) {
        super( id );
        this.title = title;
    }
}

