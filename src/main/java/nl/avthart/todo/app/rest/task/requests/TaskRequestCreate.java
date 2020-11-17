package nl.avthart.todo.app.rest.task.requests;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TaskRequestCreate extends TaskRequest {
    @SuppressWarnings("unused")
    public TaskRequestCreate( String title ) {
        super( title );
    }
}
