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
import org.axonframework.commandhandling.CommandHandler;
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

    private boolean completed;

    /**
     * Creates a new Task.
     *
     * @param command create Task
     */
    @CommandHandler
    public TaskAggregate( TaskCommandCreate command ) {
        if ( "BadCmdTask".equals( command.getTitle() ) ) {
            throw new IllegalArgumentException( "TaskAggregate (command): BadCmdTask" );
        }
        Monitor.show();
        apply( new TaskEventCreated( command.getId(), command.getUsername(), command.getTitle() ) );
    }

    TaskAggregate() {
    }

    /**
     * Completes a Task.
     *
     * @param command complete Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandComplete command ) {
        apply( new TaskEventCompleted( command.getId() ) );
    }

    /**
     * Stars a Task.
     *
     * @param command star Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandStar command ) {
        apply( new TaskEventStarred( command.getId() ) );
    }

    /**
     * Unstars a Task.
     *
     * @param command unstar Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandUnstar command ) {
        apply( new TaskEventUnstarred( command.getId() ) );
    }

    /**
     * Modifies a Task title.
     *
     * @param command modify Task title
     */
    @SuppressWarnings("unused")
    @CommandHandler
    void on( TaskCommandModifyTitle command ) {
        assertNotCompleted();
        apply( new TaskEventTitleModified( command.getId(), command.getTitle() ) );
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventCreated event ) {
        if ( "BadEventTask".equals( event.getTitle() ) ) {
            throw new IllegalArgumentException( "TaskAggregate (event): BadEventTask" );
        }
        Monitor.show();
        id = event.getId();
    }

    @SuppressWarnings("unused")
    @EventSourcingHandler
    void on( TaskEventCompleted event ) {
        completed = true;
    }

    private void assertNotCompleted() {
        if ( completed ) {
            throw new TaskAlreadyCompletedException( "Task [ identifier = " + id + " ] is completed." );
        }
    }
}
