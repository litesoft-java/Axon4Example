package nl.avthart.todo.app.domain.task.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import nl.avthart.todo.app.common.util.IdSupplier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public abstract class TaskCommand implements IdSupplier<String>  {
    @TargetAggregateIdentifier
    protected final String id;
}
