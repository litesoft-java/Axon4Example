package nl.avthart.todo.app.domain.task;

import java.util.Map;

import nl.avthart.todo.app.domain.load.Loadable;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import org.springframework.stereotype.Component;

@Component
public class TaskLoadable implements Loadable<TaskCommandLoad> {
    static final String LOADED = "loaded";

    enum field {
        id {
            @Override
            @SuppressWarnings("unchecked")
            String convert( Object value ) {
                return Loadable.asString( value, name() ); // Optional
            }
        },
        username {
            @Override
            @SuppressWarnings("unchecked")
            String convert( Object value ) {
                return Loadable.defaultString( Loadable.asString( value, name() ), LOADED );
            }
        },
        createdHour {
            @Override
            @SuppressWarnings("unchecked")
            String convert( Object value ) {
                return Loadable.optionalRegex(Loadable.asString( value, name() ), name(), "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}Z$");
            }
        },
        title {
            @Override
            @SuppressWarnings("unchecked")
            String convert( Object value ) {
                return Loadable.requiredString(Loadable.asString( value, name() ), name());
            }
        },
        starred {
            @Override
            @SuppressWarnings("unchecked")
            Boolean convert( Object value ) {
                return Boolean.TRUE.equals( Loadable.asBoolean( value, name() ) );
            }
        },
        completed {
            @Override
            @SuppressWarnings("unchecked")
            Boolean convert( Object value ) {
                return Boolean.TRUE.equals( Loadable.asBoolean( value, name() ) );
            }
        };

        <T> T from( Map<String, ?> map) {
            return convert( map.get( name() ) );
        }

        abstract <T> T convert(Object value);
    }
    @Override
    public String type() {
        return "Task";
    }

    @Override
    public TaskCommandLoad createCommand( Map<String, ?> map ) {
        return TaskCommandLoad.builder()
                .username( field.username.from(map) )
                .createdHour( field.createdHour.from(map) )
                .title( field.title.from(map) )
                .starred( field.starred.from(map) )
                .completed( field.completed.from(map) )
                .build();
    }
}
