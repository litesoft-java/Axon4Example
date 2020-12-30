package nl.avthart.todo.app.query.task;

import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.avthart.todo.app.common.util.FieldAccessors;

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

    private static final FieldAccessors<AbstractTaskEntry_v001> FIELD_ACCESSORS =
            FieldAccessors.builder( AbstractTaskEntry_v001.class )
                    .add( AbstractTaskEntry_v001::getCreatedHour, "createdHour" )
                    .add( AbstractTaskEntry_v001::getUsername, "username" )
                    .add( AbstractTaskEntry_v001::getTitle, "title" )
                    .add( AbstractTaskEntry_v001::isCompleted, "completed" )
                    .add( AbstractTaskEntry_v001::isStarred, "starred" )
                    .build();

    protected AbstractTaskEntry_v001( AbstractTaskEntry_v001 fields ) {
        this( fields.createdHour,
              fields.username,
              fields.title,
              fields.completed,
              fields.starred );
    }

    public boolean isEquivalent( AbstractTaskEntry_v001 them ) {
        return FIELD_ACCESSORS.areEquivalent( this, them );
    }

    public String delta( AbstractTaskEntry_v001 them ) {
        return FIELD_ACCESSORS.delta( this, them );
    }
}