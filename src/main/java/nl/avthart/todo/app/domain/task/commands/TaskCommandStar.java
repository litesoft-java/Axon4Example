package nl.avthart.todo.app.domain.task.commands;

import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class TaskCommandStar implements TaskCommand {
    @TargetAggregateIdentifier
    String id;
}