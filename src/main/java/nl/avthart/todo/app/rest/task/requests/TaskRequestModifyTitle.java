package nl.avthart.todo.app.rest.task.requests;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TaskRequestModifyTitle extends TaskRequest {
	@SuppressWarnings("unused")
	public TaskRequestModifyTitle( String title ) {
		super( title );
	}
}
