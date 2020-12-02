package nl.avthart.todo.app.query.task;

import java.time.Instant;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.avthart.todo.app.common.axon.Projection;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = {"id"})
@MappedSuperclass
public class TaskEntity extends AbstractTaskEntry_v001 implements Projection<String> {

    @Id
    protected String id;

    @Version
    protected Long version;

    protected Instant lastModified;

    @Builder(toBuilder = true)
    public TaskEntity( String id, Long version, Instant lastModified, String createdHour, String username, String title, boolean completed, boolean starred ) {
        super( createdHour, username, title, completed, starred );
        this.id = id;
        this.version = version;
        this.lastModified = lastModified;
    }

    protected TaskEntity( TaskEntity them ) {
        this( them.id, them.version, them.lastModified, them.createdHour, them.username, them.title, them.completed, them.starred );
    }
}