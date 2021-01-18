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
                    .add( "createdHour",
                          AbstractTaskEntry_v001::getCreatedHour,
                          AbstractTaskEntry_v001::setCreatedHour )
                    .add( "username",
                          AbstractTaskEntry_v001::getUsername,
                          AbstractTaskEntry_v001::setUsername )
                    .add( "title",
                          AbstractTaskEntry_v001::getTitle,
                          AbstractTaskEntry_v001::setTitle )
                    .add( "completed",
                          AbstractTaskEntry_v001::isCompleted,
                          AbstractTaskEntry_v001::setCompleted )
                    .add( "starred",
                          AbstractTaskEntry_v001::isStarred,
                          AbstractTaskEntry_v001::setStarred )
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

    public void updateFrom( AbstractTaskEntry_v001 them ) {
        FIELD_ACCESSORS.updateFirst( this, them );
    }

    public void defaultFrom( AbstractTaskEntry_v001 them ) {
        FIELD_ACCESSORS.defaultFromSecond( this, them );
    }
}