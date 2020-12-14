package nl.avthart.todo.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.avthart.todo.app.domain.task.TaskLoadable;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import nl.avthart.todo.app.query.task.TaskActive;
import nl.avthart.todo.app.rest.task.TaskRequestHandler;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.fail;

public class AbstractTaskTestSupport {
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static Map<String, ?> populate( Object... keyValues ) {
        Map map = new HashMap<>();
        if ( keyValues != null ) {
            if ( (keyValues.length & 1) == 1 ) { // odd number of keyValues?
                throw new IllegalArgumentException( "Missing Value for key: " + keyValues[keyValues.length - 1] );
            }
            for ( int i = 0; i < keyValues.length; i += 2 ) {
                Object key = keyValues[i];
                if ( !(key instanceof String) ) {
                    throw new IllegalArgumentException( "keyValues[" + i + "] not a string (" +
                                                        ((key == null) ? "null" : key.getClass().getSimpleName()) + "): " + key );
                }
                Object value = keyValues[i + 1];
                if ( value != null ) {
                    map.put( key, value );
                }
            }
        }
        return (Map<String, ?>)map;
    }

    protected static TaskActive get( TaskRequestHandler handler, String user, boolean completed, int expected, String taskIdOfInterest ) {
        Page<TaskActive> page = handler.findAll( user, completed, null );
        assertNotNull( page );
        List<TaskActive> tasks = page.getContent();
        assertNotNull( tasks );
        if ( tasks.size() < expected ) {
            fail( "Expected at least " + expected + " tasks, but only got: " + tasks.size() );
        }
        TaskActive task = null;
        for ( TaskActive active : tasks ) {
            if (active.getId().equals( taskIdOfInterest )) {
                task = active;
                break;
            }
        }
        if (expected != 0) {
            assertNotNull( "Task " + taskIdOfInterest + " Not found", task );
        } else {
            assertNull( "Task " + taskIdOfInterest + " should NOT have been found", task );
        }
        return task;
    }
}