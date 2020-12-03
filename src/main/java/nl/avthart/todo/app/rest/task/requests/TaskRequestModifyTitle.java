package nl.avthart.todo.app.rest.task.requests;

import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TaskRequestModifyTitle extends TaskTitleRequest {
	@SuppressWarnings("unused")
	@Builder
	public TaskRequestModifyTitle( String title ) {
		super( title );
	}
}
