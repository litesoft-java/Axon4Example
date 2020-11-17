package nl.avthart.todo.app.query.task;

import java.sql.SQLException;
import java.util.function.Consumer;

import nl.avthart.todo.app.configuration.PrimaryProjector;
import nl.avthart.todo.app.domain.task.events.TaskEventCompleted;
import nl.avthart.todo.app.domain.task.events.TaskEventCreated;
import nl.avthart.todo.app.domain.task.events.TaskEventStarred;
import nl.avthart.todo.app.domain.task.events.TaskEventTitleModified;
import nl.avthart.todo.app.domain.task.events.TaskEventUnstarred;
import nl.avthart.todo.app.flags.Monitor;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author albert
 */
@Component
public class TaskEntryUpdatingEventHandler implements PrimaryProjector {

    private final TaskEntryRepository taskEntryRepository;

    @Autowired
    public TaskEntryUpdatingEventHandler( TaskEntryRepository taskEntryRepository ) {
        this.taskEntryRepository = taskEntryRepository;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCreated event, UnitOfWork<?> uow ) {
        if ( "BadTask".equals( event.getTitle() ) ) {
            throw new ConstraintViolationException( "TaskEntryUpdatingEventHandler: BadTask",
                                                    new SQLException(), "Ginger" );
        }
        Monitor.show();
        TaskEntry task = new TaskEntry( event.getId(), event.getUsername(), event.getTitle(), false, false );
        taskEntryRepository.save( task );
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCompleted event ) {
        updateTaskEntry( event.getId(), task -> task.setCompleted( true ) );
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventTitleModified event ) {
        updateTaskEntry( event.getId(), task -> task.setTitle( event.getTitle() ) );
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventStarred event ) {
        updateTaskEntry( event.getId(), task -> task.setStarred( true ) );
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUnstarred event ) {
        updateTaskEntry( event.getId(), task -> task.setStarred( false ) );
    }

    private void updateTaskEntry( String pTaskId, Consumer<TaskEntry> pProcessor ) {
        TaskEntry task = taskEntryRepository.findById( pTaskId ).orElse( null );
        if ( task == null ) {
            throw new IllegalArgumentException( "Task '" + pTaskId + "' not found" );
        }
        pProcessor.accept( task );
        taskEntryRepository.save( task );
    }
}
