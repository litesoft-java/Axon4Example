package nl.avthart.todo.app.domain.task.commands;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class TaskCommandLoad extends TaskCommand {
    private final boolean overwrite; // Loader should overwrite existing row!

    private final String createdHour; // 2020-11-19T13Z
    private final String username;
    private final String title;
    private final boolean completed;
    private final boolean starred;

    @Builder(toBuilder = true)
    public TaskCommandLoad( String id, boolean overwrite,
                            String createdHour,
                            String username,
                            String title,
                            boolean completed,
                            boolean starred ) {
        super( id );
        this.overwrite = overwrite;

        this.createdHour = createdHour;
        this.username = username;
        this.title = title;
        this.completed = completed;
        this.starred = starred;
    }
}
