package nl.avthart.todo.app.domain.task.commands;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
@AllArgsConstructor
public class TaskCommandCreate implements TaskCommand {
    @TargetAggregateIdentifier
    String id;

    @NotNull
    String username;

    @NotNull
    String title;
}
