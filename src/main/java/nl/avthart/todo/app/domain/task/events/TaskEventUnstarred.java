package nl.avthart.todo.app.domain.task.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventUnstarred implements TaskEvent {

    String id;
}
