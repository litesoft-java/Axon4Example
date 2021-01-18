package nl.avthart.todo.app.notify.task;

import nl.avthart.todo.app.domain.task.events.TaskEvent;
import nl.avthart.todo.app.domain.task.events.TaskEventCompleted;
import nl.avthart.todo.app.domain.task.events.TaskEventCreated;
import nl.avthart.todo.app.domain.task.events.TaskEventStarred;
import nl.avthart.todo.app.domain.task.events.TaskEventTitleModified;
import nl.avthart.todo.app.domain.task.events.TaskEventUnstarred;
import nl.avthart.todo.app.flags.Monitor;
import nl.avthart.todo.app.query.task.TaskActive;
import nl.avthart.todo.app.query.task.TaskPrimaryProjectionReadRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

/**
 * @author albert
 */
@Component
public class TaskEventNotifyingEventHandler {

    private final SimpMessageSendingOperations messagingTemplate;

    private final TaskPrimaryProjectionReadRepository repo;

    @Autowired
    public TaskEventNotifyingEventHandler( SimpMessageSendingOperations messagingTemplate, TaskPrimaryProjectionReadRepository repo ) {
        this.messagingTemplate = messagingTemplate;
        this.repo = repo;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCreated event, @SequenceNumber long version ) {
        Monitor.show();
        readAndPublish( event, version ); // because the PrimaryProjector is updated synchronously, we can rely on the repository to be updated already
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCompleted event, @SequenceNumber long version ) {
        readAndPublish( event, version );
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventTitleModified event, @SequenceNumber long version ) {
        readAndPublish( event, version );
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventStarred event, @SequenceNumber long version ) {
        readAndPublish( event, version );
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUnstarred event, @SequenceNumber long version ) {
        readAndPublish( event, version );
    }

    private void readAndPublish( TaskEvent event, long version ) {
        String zId = event.getId();
        TaskActive task = repo.findActiveById( zId );
        if ( task == null ) {
            new IllegalStateException( "Task '" + zId + "' not found" ).printStackTrace();
            return;
        }
        String username = task.getUsername();
        String type = event.getClass().getSimpleName();
        System.out.println( "************ Published: " + type + " (" + version + ")" );
        this.messagingTemplate.convertAndSendToUser( username, "/queue/task-updates",
                                                     new TaskEventNotification( type, event ) );
    }
}
