package nl.avthart.todo.app.domain.task.events;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.avthart.todo.app.query.task.AbstractTaskEntry_v001;

@NoArgsConstructor
@ToString(callSuper = true)
public class TaskEventUpdated extends AbstractAllFieldsTaskEvent implements TaskEvent {
    @SuppressWarnings("unused")
    @Builder
    public TaskEventUpdated( String id,
                             String createdHour,
                             String username,
                             String title,
                             boolean completed,
                             boolean starred ) {
        super( id, createdHour, username, title, completed, starred );
    }

    public TaskEventUpdated( String id, AbstractTaskEntry_v001 fields ) {
        super( id, fields );
    }
}
