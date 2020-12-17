package nl.avthart.todo.app.rest.task.requests;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class TaskTitleRequest {
    @NotNull
    private String title;
}
