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

    @SuppressWarnings("unused")
    @Builder(toBuilder = true)
    public TaskEntity( String id, Long version, Instant lastModified, String createdHour, String username, String title, boolean completed, boolean starred ) {
        super( createdHour, username, title, completed, starred );
        update( id, version, lastModified );
    }

    public TaskEntity( String id, Long version, Instant lastModified, AbstractTaskEntry_v001 them ) {
        super( them );
        update( id, version, lastModified );
    }

    protected TaskEntity( TaskEntity them ) {
        super( them );
        update( them.id, them.version, them.lastModified );
    }

    private void update( String id, Long version, Instant lastModified ) {
        this.id = id;
        this.version = version;
        this.lastModified = lastModified;

        if ( getCreatedHour() == null ) {
            setCreatedHour( toHour( (lastModified != null) ? lastModified : Instant.now() ) );
        }
    }

    private String toHour( Instant instant ) {
        // 2020-11-19T13Z
        // 01234567-10123
        // 2020-11-19T13:12:11.101Z
        return instant.toString().substring( 0, 13 ) + "Z";
    }
}
