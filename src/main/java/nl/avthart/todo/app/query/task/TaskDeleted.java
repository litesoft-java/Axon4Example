package nl.avthart.todo.app.query.task;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.avthart.todo.app.common.axon.DeletedProjection;

@Entity
@NoArgsConstructor
@ToString(callSuper = true)
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "TaskEntryDeleted", uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "lastModified", "id"})})
public class TaskDeleted extends TaskEntity implements DeletedProjection<String> {
    public TaskDeleted( TaskEntity them ) {
        super( them );
    }
}