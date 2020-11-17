package nl.avthart.todo.app.notify.task;

import java.time.Instant;

import nl.avthart.todo.app.domain.task.events.TaskEvent;
import nl.avthart.todo.app.domain.task.events.TaskEventCompleted;
import nl.avthart.todo.app.domain.task.events.TaskEventCreated;
import nl.avthart.todo.app.domain.task.events.TaskEventStarred;
import nl.avthart.todo.app.domain.task.events.TaskEventTitleModified;
import nl.avthart.todo.app.domain.task.events.TaskEventUnstarred;
import nl.avthart.todo.app.query.task.TaskEntry;
import nl.avthart.todo.app.query.task.TaskEntryRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import sun.jvmstat.monitor.MonitorException;

/**
 * @author albert
 */
@Component
public class TaskEventNotifyingEventHandler {

    private final SimpMessageSendingOperations messagingTemplate;

    private final TaskEntryRepository taskEntryRepository;

    @Autowired
    public TaskEventNotifyingEventHandler( SimpMessageSendingOperations messagingTemplate, TaskEntryRepository taskEntryRepository ) {
        this.messagingTemplate = messagingTemplate;
        this.taskEntryRepository = taskEntryRepository;
    }

	@SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCreated event ) {
        new MonitorException( Instant.now().toString() ).printStackTrace();
        publish( event.getUsername(), event );
    }

	@SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCompleted event ) {
		readAndPublish( event );
    }

	@SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventTitleModified event ) {
		readAndPublish( event );
    }

	@SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventStarred event ) {
		readAndPublish( event );
    }

	@SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUnstarred event ) {
        readAndPublish( event );
    }

    private void readAndPublish( TaskEvent event ) {
        String zId = event.getId();
        TaskEntry task = taskEntryRepository.findById( zId ).orElse( null );
        if ( task != null ) {
            publish( task.getUsername(), event );
        } else {
            new IllegalStateException( "Task '" + zId + "' not found" ).printStackTrace();
        }
    }

    private void publish( String username, TaskEvent event ) {
        String type = event.getClass().getSimpleName();
        this.messagingTemplate.convertAndSendToUser( username, "/queue/task-updates", new TaskEventNotification( type, event ) );
    }
}
