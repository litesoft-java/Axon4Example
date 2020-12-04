package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import nl.avthart.todo.app.query.task.AbstractTaskEntry_v001;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractTaskCommandLoad extends AbstractTaskEntry_v001 implements TaskCommand {
    @TargetAggregateIdentifier
    String id;

    protected AbstractTaskCommandLoad( String id,
                                    String createdHour,
                                    String username,
                                    String title,
                                    boolean completed,
                                    boolean starred ) {
        super( createdHour, username, title, completed, starred);
        this.id = id;
    }
}
