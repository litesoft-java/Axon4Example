package nl.avthart.todo.app.domain.task.update;

import lombok.Getter;
import nl.avthart.todo.app.common.axon.AggregateObject;
import nl.avthart.todo.app.domain.task.TaskAlreadyCompletedException;
import nl.avthart.todo.app.domain.task.commands.TaskCommandComplete;
import nl.avthart.todo.app.domain.task.commands.TaskCommandCreate;
import nl.avthart.todo.app.domain.task.commands.TaskCommandDelete;
import nl.avthart.todo.app.domain.task.commands.TaskCommandModifyTitle;
import nl.avthart.todo.app.domain.task.commands.TaskCommandRestore;
import nl.avthart.todo.app.domain.task.commands.TaskCommandStar;
import nl.avthart.todo.app.domain.task.commands.TaskCommandUnstar;
import nl.avthart.todo.app.domain.task.commands.TaskCommandUpdate;
import nl.avthart.todo.app.domain.task.events.TaskEventCompleted;
import nl.avthart.todo.app.domain.task.events.TaskEventCreated;
import nl.avthart.todo.app.domain.task.events.TaskEventDeleted;
import nl.avthart.todo.app.domain.task.events.TaskEventRestored;
import nl.avthart.todo.app.domain.task.events.TaskEventStarred;
import nl.avthart.todo.app.domain.task.events.TaskEventTitleModified;
import nl.avthart.todo.app.domain.task.events.TaskEventUnstarred;
import nl.avthart.todo.app.domain.task.events.TaskEventUpdated;
import nl.avthart.todo.app.flags.Monitor;
import nl.avthart.todo.app.query.task.AbstractTaskEntry_v001;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.IdentifierFactory;
import org.axonframework.eventhandling.SequenceNumber;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * Task
 */
@Aggregate
@Getter
public class TaskAggregate extends AbstractTaskEntry_v001 implements AggregateObject<String> {

    /**
     * The constant serialVersionUID
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -5977984483620451665L;

    @AggregateIdentifier
    private String id;

    // version (sequence number) is required for syncProcess calls
    private long version; // defaults to 0 (appropriate value for create)

    private boolean deleted; // defaults to false (appropriate value for create)

    TaskAggregate() { // Required for rehydration
    }

    /**
     * Creates a new Task.
     *
     * @param command          create Task
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @CommandHandler
    public TaskAggregate( TaskCommandCreate command, TaskPrimaryProjector primaryProjector ) {
        // vvv Testing
        if ( "BadCmdTask".equals( command.getTitle() ) ) {
            throw new IllegalArgumentException( "TaskAggregate (command): BadCmdTask" );
        }
        Monitor.show();
        // ^^^ Testing
        String id = command.getId();
        if ( id == null ) {
            id = IdentifierFactory.getInstance().generateIdentifier();
        }
        apply( primaryProjector.syncProcess( null,
                                             new TaskEventCreated( id, command ) ) );
    }

    /**
     * Update (load/overwrite) a Task.
     *
     * @param command          update Task
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandUpdate command, TaskPrimaryProjector primaryProjector ) {
        apply( primaryProjector.syncProcess( this, null,
                                             new TaskEventUpdated( command.getId(), command ) ) );
    }

    /**
     * Stars a Task.
     *
     * @param command          star Task
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandStar command, TaskPrimaryProjector primaryProjector ) {
        apply( primaryProjector.syncProcess( assertOkForUpdate(), null,
                                             new TaskEventStarred( command.getId() ) ) );
    }

    /**
     * Unstars a Task.
     *
     * @param command          unstar Task
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandUnstar command, TaskPrimaryProjector primaryProjector ) {
        apply( primaryProjector.syncProcess( assertOkForUpdate(), null,
                                             new TaskEventUnstarred( command.getId() ) ) );
    }

    /**
     * Modifies a Task title.
     *
     * @param command          modify Task title
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandModifyTitle command, TaskPrimaryProjector primaryProjector ) {
        apply( primaryProjector.syncProcess( assertOkForUpdate(), null,
                                             new TaskEventTitleModified( command.getId(),
                                                                         command.getTitle() ) ) );
    }

    /**
     * Completes a Task.
     *
     * @param command          complete Task
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandComplete command, TaskPrimaryProjector primaryProjector ) {
        apply( primaryProjector.syncProcess( assertOkForUpdate(), null,
                                             new TaskEventCompleted( command.getId() ) ) );
    }

    /**
     * Delete a Task.
     *
     * @param command          delete Task
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandDelete command, TaskPrimaryProjector primaryProjector ) {
        apply( primaryProjector.syncProcess( this, null,
                                             new TaskEventDeleted( command.getId() ) ) );
    }

    /**
     * Restore a Task.
     *
     * @param command          restore Task
     * @param primaryProjector support syncProcess of the the converted (to event) command
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandRestore command, TaskPrimaryProjector primaryProjector ) {
        apply( primaryProjector.syncProcess( this, null,
                                             new TaskEventRestored( command.getId() ) ) );
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventCreated event ) {
        // vvv Testing
        Monitor.show();
        // ^^^ Testing
        id = event.getId();
        completed = event.isCompleted();
        // event.getUsername() not persisted in this Aggregate
        // event.getTitle() not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventUpdated event, @SequenceNumber long version ) {
        this.version = version;
        this.completed = event.isCompleted();
        // Other fields not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventStarred event, @SequenceNumber long version ) {
        this.version = version;
        // Starred status not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventUnstarred event, @SequenceNumber long version ) {
        this.version = version;
        // Starred status not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventTitleModified event, @SequenceNumber long version ) {
        this.version = version;
        // event.getTitle() not persisted in this Aggregate
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventCompleted event, @SequenceNumber long version ) {
        this.version = version;
        completed = true;
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventDeleted event, @SequenceNumber long version ) {
        this.version = version;
        deleted = true;
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventRestored event, @SequenceNumber long version ) {
        this.version = version;
        deleted = false;
    }

    private TaskAggregate assertOkForUpdate() {
        if ( completed ) {
            throw new TaskAlreadyCompletedException( "Task [ identifier = " + id + " ] is completed." );
        }
        return this;
    }
}
