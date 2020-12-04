package nl.avthart.todo.app.query.task;

import java.util.Objects;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractTaskEntry_v001 {
    protected String createdHour; // 2020-11-19T13Z
    protected String username;
    protected String title;
    protected boolean completed;
    protected boolean starred;

    protected AbstractTaskEntry_v001( AbstractTaskEntry_v001 fields ) {
        this( fields.createdHour,
              fields.username,
              fields.title,
              fields.completed,
              fields.starred );
    }

    @SuppressWarnings("unused")
    public boolean isEquivalent( AbstractTaskEntry_v001 them ) {
        return (them != null)
               && Objects.equals( this.createdHour, them.createdHour )
               && Objects.equals( this.username, them.username )
               && Objects.equals( this.title, them.title )
               && Objects.equals( this.completed, them.completed )
               && Objects.equals( this.starred, them.starred )
                ;
    }
}