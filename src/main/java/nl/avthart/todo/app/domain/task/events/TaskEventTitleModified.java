package nl.avthart.todo.app.domain.task.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventTitleModified implements TaskEvent {

	String id;
	
	String title;
}
