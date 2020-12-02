package nl.avthart.todo.app.query.task;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.avthart.todo.app.common.axon.ActiveProjection;

@Entity
@NoArgsConstructor
@ToString(callSuper = true)
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "TaskEntry", uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "createdHour", "title"})})
public class TaskActive extends TaskEntity implements ActiveProjection<String> {
    public TaskActive( TaskEntity them ) {
        super( them );
    }
}