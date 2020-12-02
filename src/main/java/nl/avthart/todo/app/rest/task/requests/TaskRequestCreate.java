package nl.avthart.todo.app.rest.task.requests;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskRequestCreate extends TaskTitleRequest {
    @SuppressWarnings("unused")
    @Builder
    public TaskRequestCreate( String title ) {
        super( title );
    }
}
