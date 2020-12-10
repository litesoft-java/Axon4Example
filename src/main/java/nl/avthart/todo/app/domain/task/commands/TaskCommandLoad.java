package nl.avthart.todo.app.domain.task.commands;

import lombok.Builder;
import lombok.ToString;
import nl.avthart.todo.app.domain.load.LoadCommand;

@ToString(callSuper = true)
public class TaskCommandLoad extends AbstractTaskCommandLoad implements LoadCommand {

    @Builder(toBuilder = true)
    public TaskCommandLoad( String id,
                            String createdHour,
                            String username,
                            String title,
                            boolean completed,
                            boolean starred ) {
        super( id, createdHour, username, title, completed, starred );
    }
}
