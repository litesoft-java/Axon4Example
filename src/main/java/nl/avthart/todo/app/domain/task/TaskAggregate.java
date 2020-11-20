package nl.avthart.todo.app.domain.task;

import nl.avthart.todo.app.domain.task.commands.TaskCommandComplete;
import nl.avthart.todo.app.domain.task.commands.TaskCommandCreate;
import nl.avthart.todo.app.domain.task.commands.TaskCommandModifyTitle;
import nl.avthart.todo.app.domain.task.commands.TaskCommandStar;
import nl.avthart.todo.app.domain.task.commands.TaskCommandUnstar;
import nl.avthart.todo.app.domain.task.events.TaskEventCompleted;
import nl.avthart.todo.app.domain.task.events.TaskEventCreated;
import nl.avthart.todo.app.domain.task.events.TaskEventStarred;
import nl.avthart.todo.app.domain.task.events.TaskEventTitleModified;
import nl.avthart.todo.app.domain.task.events.TaskEventUnstarred;
import nl.avthart.todo.app.flags.Monitor;
import nl.avthart.todo.app.query.task.TaskEntryUpdatingEventHandler;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * Task
 *
 * @author albert
 */
@Aggregate
public class TaskAggregate {

    /**
     * The constant serialVersionUID
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -5977984483620451665L;

    @AggregateIdentifier
    private String id;

    // version (sequence number) is required for syncProcess calls
    private long version; // defaults to 0 (appropriate value for create)

    // Other fields... None of these are actually required to be here, except for the purpose of command validation
    private boolean completed; // defaults to false

    /**
     * Creates a new Task.
     *
     * @param command create Task
     */
    @CommandHandler
    public TaskAggregate( TaskCommandCreate command, TaskEntryUpdatingEventHandler primaryProjector ) {
        if ( "BadCmdTask".equals( command.getTitle() ) ) {
            throw new IllegalArgumentException( "TaskAggregate (command): BadCmdTask" );
        }
        Monitor.show();
        apply( primaryProjector.syncProcess( new TaskEventCreated( command.getId(),
                                                                   command.getUsername(),
                                                                   command.getTitle() ) ) );
    }

    TaskAggregate() { // Required for rehydration
    }

    /**
     * Completes a Task.
     *
     * @param command complete Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandComplete command, TaskEntryUpdatingEventHandler primaryProjector ) {
        assertNotCompleted();
        apply( primaryProjector.syncProcess( version, new TaskEventCompleted( command.getId() ) ) );
    }

    /**
     * Stars a Task.
     *
     * @param command star Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandStar command, TaskEntryUpdatingEventHandler primaryProjector ) {
        assertNotCompleted();
        apply( primaryProjector.syncProcess( version, new TaskEventStarred( command.getId() ) ) );
    }

    /**
     * Unstars a Task.
     *
     * @param command unstar Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandUnstar command, TaskEntryUpdatingEventHandler primaryProjector ) {
        assertNotCompleted();
        apply( primaryProjector.syncProcess( version, new TaskEventUnstarred( command.getId() ) ) );
    }

    /**
     * Modifies a Task title.
     *
     * @param command modify Task title
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandModifyTitle command, TaskEntryUpdatingEventHandler primaryProjector ) {
        assertNotCompleted();
        apply( primaryProjector.syncProcess( version, new TaskEventTitleModified( command.getId(),
                                                                                  command.getTitle() ) ) );
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventCreated event ) {
        Monitor.show();
        id = event.getId();
        // event.getUsername() not persisted in this Aggregate
        // event.getTitle() not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventCompleted event, @SequenceNumber long version ) {
        this.version = version;
        completed = true;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventTitleModified event, @SequenceNumber long version ) {
        this.version = version;
        // event.getTitle() not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventStarred event, @SequenceNumber long version ) {
        this.version = version;
        // Starred status not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUnstarred event, @SequenceNumber long version ) {
        this.version = version;
        // Starred status not persisted in this Aggregate
    }

    private void assertNotCompleted() {
        if ( completed ) {
            throw new TaskAlreadyCompletedException( "Task [ identifier = " + id + " ] is completed." );
        }
    }
}
