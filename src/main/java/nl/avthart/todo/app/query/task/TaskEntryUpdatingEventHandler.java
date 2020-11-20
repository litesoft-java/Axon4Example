package nl.avthart.todo.app.query.task;

import java.sql.SQLException;
import java.time.Instant;
import javax.persistence.OptimisticLockException;

import nl.avthart.todo.app.configuration.PrimaryProjector;
import nl.avthart.todo.app.domain.task.events.TaskEvent;
import nl.avthart.todo.app.domain.task.events.TaskEventCompleted;
import nl.avthart.todo.app.domain.task.events.TaskEventCreated;
import nl.avthart.todo.app.domain.task.events.TaskEventStarred;
import nl.avthart.todo.app.domain.task.events.TaskEventTitleModified;
import nl.avthart.todo.app.domain.task.events.TaskEventUnstarred;
import nl.avthart.todo.app.flags.Monitor;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * @author albert
 */
@Component
public class TaskEntryUpdatingEventHandler implements PrimaryProjector {

    static final String DUPLICATE_TASK = "Duplicate Task";

    private final TaskEntryRepository taskEntryRepository;

    @Autowired
    public TaskEntryUpdatingEventHandler( TaskEntryRepository taskEntryRepository ) {
        this.taskEntryRepository = taskEntryRepository;
    }

    public TaskEventCreated syncProcess( TaskEventCreated event ) {
        if ( null != readTask( event ) ) {
            throw new ConstraintViolationException( DUPLICATE_TASK,
                                                    new SQLException( DUPLICATE_TASK ), DUPLICATE_TASK );
        }
        if ( "BadTask".equals( event.getTitle() ) ) {
            throw new ConstraintViolationException( "TaskEntryUpdatingEventHandler: BadTask",
                                                    new SQLException(), "Ginger" );
        }
        Monitor.show();
        try {
            createTask( event );
        }
        catch ( DataIntegrityViolationException | ConstraintViolationException e ) {
            throw e;
        }
        catch ( RuntimeException e ) {
            e.printStackTrace();
            throw e;
        }
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCreated event ) {
        if ( null == readTask( event ) ) {
            createTask( event );
        }
    }

    private void createTask( TaskEventCreated event ) {
        taskEntryRepository.save( TaskEntry.builder()
                                          .id( event.getId() )
                                          .version( 0L )
                                          .createdHour( nowHour() )
                                          .username( event.getUsername() )
                                          .title( event.getTitle() )
                                          .completed( false )
                                          .starred( false )
                                          .build() );
    }

    private String nowHour() {
        // 2020-11-19T13Z
        // 01234567-10123
        // 2020-11-19T13:12:11.101Z
        return Instant.now().toString().substring( 0, 13 ) + "Z";
    }

    public TaskEventCompleted syncProcess( long currentVersion, TaskEventCompleted event ) {
        update( event, checkSyncUpdate( currentVersion, event ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCompleted event, @SequenceNumber long nextVersion ) {
        update( event, checkHandlerUpdate( nextVersion, event ) );
    }

    @SuppressWarnings("unused")
    private void update( TaskEventCompleted event, TaskEntry task ) {
        if ( task != null ) {
            task.setCompleted( true );
            updateTaskEntry( task );
        }
    }

    public TaskEventTitleModified syncProcess( long currentVersion, TaskEventTitleModified event ) {
        TaskEntry task = checkSyncUpdate( currentVersion, event );
        if ( "BadVersion".equals( event.getTitle() ) && task.isStarred() ) {
            task = task.toBuilder().version( 0L ).build();
        }
        update( event, task );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventTitleModified event, @SequenceNumber long nextVersion ) {
        update( event, checkHandlerUpdate( nextVersion, event ) );
    }

    private void update( TaskEventTitleModified event, TaskEntry task ) {
        if ( task != null ) {
            task.setTitle( event.getTitle() );
            updateTaskEntry( task );
        }
    }

    public TaskEventStarred syncProcess( long currentVersion, TaskEventStarred event ) {
        update( event, checkSyncUpdate( currentVersion, event ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventStarred event, @SequenceNumber long nextVersion ) {
        update( event, checkHandlerUpdate( nextVersion, event ) );
    }

    @SuppressWarnings("unused")
    private void update( TaskEventStarred event, TaskEntry task ) {
        if ( task != null ) {
            task.setStarred( true );
            updateTaskEntry( task );
        }
    }

    public TaskEventUnstarred syncProcess( long currentVersion, TaskEventUnstarred event ) {
        TaskEntry task = checkSyncUpdate( currentVersion, event );
        if ( !"SkipUnstar".equals( task.getTitle() ) ) {
            update( event, task );
        }
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUnstarred event, @SequenceNumber long nextVersion ) {
        update( event, checkHandlerUpdate( nextVersion, event ) );
    }

    @SuppressWarnings("unused")
    private void update( TaskEventUnstarred event, TaskEntry task ) {
        if ( task != null ) {
            task.setStarred( false );
            updateTaskEntry( task );
        }
    }

    private TaskEntry checkSyncUpdate( long currentVersion, TaskEvent event ) {
        TaskEntry task = readTask( event );
        if ( task == null ) {
            throw new IllegalStateException( // TODO: XXX
                                             "Request Task (" + event.getId() + ") to update does not exist" );
        }
        if ( task.getVersion() != currentVersion ) {
            throw versionError( task, null );
        }
        return task;
    }

    private TaskEntry checkHandlerUpdate( long nextVersion, TaskEvent event ) {
        TaskEntry task = readTask( event );
        if ( (task == null) || (task.getVersion() != (nextVersion - 1)) ) {
            return null;
        }
        String type = event.getClass().getSimpleName();
        System.out.println( "************ Update (Async): " + type );
        return task;
    }

    private TaskEntry readTask( TaskEvent pTaskEvent ) {
        return taskEntryRepository.findById( pTaskEvent.getId() ).orElse( null );
    }

    private void updateTaskEntry( TaskEntry task ) {
        try {
            taskEntryRepository.save( task );
        }
        catch ( ObjectOptimisticLockingFailureException | StaleObjectStateException | OptimisticLockException e ) {
            throw versionError( task, e );
        }
        catch ( RuntimeException e ) {
            e.printStackTrace();
            throw e;
        }
    }

    private OptimisticLockException versionError( TaskEntry task, Exception cause ) {
        String msg = "Task (" + task.getId() + ") data has changed (version mis-match)";
        return (cause != null) ?
               new OptimisticLockException( msg, cause ) :
               new OptimisticLockException( msg );
    }
}
