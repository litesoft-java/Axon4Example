package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public abstract class TaskCommand {
    @TargetAggregateIdentifier
    private final String id;
}
