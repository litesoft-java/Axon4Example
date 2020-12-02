package nl.avthart.todo.app.rest.task.requests;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TaskRequestModifyTitle extends TaskTitleRequest {
	@SuppressWarnings("unused")
	public TaskRequestModifyTitle( String title ) {
		super( title );
	}
}
