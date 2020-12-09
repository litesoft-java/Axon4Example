package nl.avthart.todo.app.domain.task.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import nl.avthart.todo.app.query.task.AbstractTaskEntry_v001;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractAllFieldsTaskCommand extends AbstractTaskEntry_v001 implements TaskCommand {
    @TargetAggregateIdentifier
    String id;

    protected AbstractAllFieldsTaskCommand( String id,
                                            String createdHour,
                                            String username,
                                            String title,
                                            boolean completed,
                                            boolean starred ) {
        super( createdHour, username, title, completed, starred );
        this.id = id;
    }

    protected AbstractAllFieldsTaskCommand( String id, AbstractTaskEntry_v001 fields ) {
        super( fields );
        this.id = id;
    }
}
