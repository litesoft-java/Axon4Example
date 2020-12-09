package nl.avthart.todo.app.domain.task.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.avthart.todo.app.query.task.AbstractTaskEntry_v001;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractAllFieldsTaskEvent extends AbstractTaskEntry_v001 {
    protected String id;

    protected AbstractAllFieldsTaskEvent( String id,
                                          String createdHour,
                                          String username,
                                          String title,
                                          boolean completed,
                                          boolean starred ) {
        super( createdHour, username, title, completed, starred );
        this.id = id;
    }

    protected AbstractAllFieldsTaskEvent( String id, AbstractTaskEntry_v001 fields ) {
        super( fields );
        this.id = id;
    }
}
