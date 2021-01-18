package nl.avthart.todo.app.domain.task.update;

import java.time.Instant;

import nl.avthart.todo.app.common.axon.AbstractPrimaryProjector;
import nl.avthart.todo.app.common.axon.AggregateObject;
import nl.avthart.todo.app.common.axon.Event;
import nl.avthart.todo.app.common.axon.LastEventReader;
import nl.avthart.todo.app.common.exceptions.BusinessRuleException;
import nl.avthart.todo.app.common.exceptions.CantUpdateException;
import nl.avthart.todo.app.domain.task.commands.AbstractTaskCommandLoad;
import nl.avthart.todo.app.domain.task.commands.TaskCommand;
import nl.avthart.todo.app.domain.task.commands.TaskCommandCreate;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoad;
import nl.avthart.todo.app.domain.task.commands.TaskCommandLoadOverwrite;
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
import nl.avthart.todo.app.query.task.TaskActive;
import nl.avthart.todo.app.query.task.TaskDeleted;
import nl.avthart.todo.app.query.task.TaskEntity;
import nl.avthart.todo.app.query.task.TaskPrimaryProjectionReadRepository;
import nl.avthart.todo.app.query.task.TaskPrimaryProjectionWriteRepository;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.stereotype.Component;

@Component
public class TaskPrimaryProjector extends AbstractPrimaryProjector<String, TaskCommand, AbstractTaskCommandLoad, TaskActive, TaskDeleted> {

    public TaskPrimaryProjector( TaskPrimaryProjectionWriteRepository repo,
                                 LastEventReader lastEventReader,
                                 CommandGateway commandGateway ) {
        super( repo, lastEventReader, commandGateway, "Task", TaskAggregate.class );
    }

    @Override
    protected String fromLastEventAggregateId( String id ) {
        return id;
    }

    /**
     * Creates (Load) a new Task.
     *
     * @param command load Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    Object on( TaskCommandLoad command ) {
        System.out.println( "************ TaskPrimaryProjector.on: " + command );
        return load( command, false );
    }

    /**
     * Creates (Load) a new Task.
     *
     * @param command load Task
     */
    @SuppressWarnings("unused")
    @CommandHandler
    Object on( TaskCommandLoadOverwrite command ) {
        System.out.println( "************ TaskPrimaryProjector.on: " + command );
        return load( command, true );
    }

    @Override
    protected TaskCommand createCreateCommand( AbstractTaskCommandLoad command ) {
        return new TaskCommandCreate( command.getId(), command );
    }

    @Override
    protected TaskCommand optionalUpdateCommand( TaskActive active, AbstractTaskCommandLoad command, boolean overwrite ) {
        command.defaultFrom( active );
        if ( active.isEquivalent( command ) ) {
            return null; // No change needed
        }
        if ( !overwrite ) {
            throw new CantUpdateException( error( active, active.delta( command ) ) );
        }
        return new TaskCommandUpdate( active.getId(), command );
    }

    // Created
    public TaskEventCreated syncProcess( Instant createdAt, TaskEventCreated event ) {
        // vvv Testing
        if ( "BadTask".equals( event.getTitle() ) ) {
            throw new BusinessRuleException( "TaskEntryUpdatingEventHandler: BadTask" );
        }
        Monitor.show();
        // ^^^ Testing
        createdAt = ensureInstant( createdAt );
        syncCreate( createdAt, event, map( event, createdAt ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCreated event, @Timestamp Instant createdAt ) {
        createdAt = ensureInstant( createdAt );
        handlerCreate( event, createdAt, map( event, createdAt ) );
    }

    private TaskActive map( TaskEventCreated event, Instant createdAt ) {
        return new TaskActive( new TaskEntity( event.getId(), 0L, createdAt, event ) );
    }

    // Updated
    public TaskEventUpdated syncProcess( AggregateObject<String> aggregate, Instant lastModifiedAt, TaskEventUpdated event ) {
        syncUpdate( lastModifiedAt, map( event, checkSyncUpdate( aggregate, event ) ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUpdated event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        handlerUpdate( map( event, checkHandlerUpdate( nextVersion, event ) ), createdAt );
    }

    private TaskActive map( TaskEventUpdated event, TaskActive entity ) {
        if ( entity != null ) {
            entity.updateFrom( event );
        }
        return entity;
    }

    // Starred
    public TaskEventStarred syncProcess( AggregateObject<String> aggregate, Instant lastModifiedAt, TaskEventStarred event ) {
        syncUpdate( lastModifiedAt, map( event, checkSyncUpdate( aggregate, event ) ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventStarred event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        handlerUpdate( map( event, checkHandlerUpdate( nextVersion, event ) ), createdAt );
    }

    @SuppressWarnings("unused")
    private TaskActive map( TaskEventStarred event, TaskActive task ) {
        if ( task != null ) {
            task.setStarred( true );
        }
        return task;
    }

    public TaskEventUnstarred syncProcess( AggregateObject<String> aggregate, Instant lastModifiedAt, TaskEventUnstarred event ) {
        TaskActive task = checkSyncUpdate( aggregate, event );
        // vvv Testing
        if ( "SkipUnstar".equals( task.getTitle() ) ) {
            return event;
        }
        // ^^^ Testing
        syncUpdate( lastModifiedAt, map( event, task ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUnstarred event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        handlerUpdate( map( event, checkHandlerUpdate( nextVersion, event ) ), createdAt );
    }

    @SuppressWarnings("unused")
    private TaskActive map( TaskEventUnstarred event, TaskActive task ) {
        if ( task != null ) {
            task.setStarred( false );
        }
        return task;
    }

    public TaskEventTitleModified syncProcess( AggregateObject<String> aggregate, Instant lastModifiedAt, TaskEventTitleModified event ) {
        TaskActive task = checkSyncUpdate( aggregate, event );
        // vvv Testing
        if ( "BadVersion".equals( event.getTitle() ) && task.isStarred() ) { // Faking user sent bad Version!
            task = new TaskActive( task.toBuilder().version( 0L ).build() ); // Forces new entity which is NOT in the ORM transaction
        }
        // ^^^ Testing
        syncUpdate( lastModifiedAt, map( event, task ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventTitleModified event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        handlerUpdate( map( event, checkHandlerUpdate( nextVersion, event ) ), createdAt );
    }

    private TaskActive map( TaskEventTitleModified event, TaskActive task ) {
        if ( task != null ) {
            task.setTitle( event.getTitle() );
        }
        return task;
    }

    public TaskEventCompleted syncProcess( AggregateObject<String> aggregate, Instant lastModifiedAt, TaskEventCompleted event ) {
        syncUpdate( lastModifiedAt, map( event, checkSyncUpdate( aggregate, event ) ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventCompleted event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        handlerUpdate( map( event, checkHandlerUpdate( nextVersion, event ) ), createdAt );
    }

    @SuppressWarnings("unused")
    private TaskActive map( TaskEventCompleted event, TaskActive task ) {
        if ( task != null ) {
            task.setCompleted( true );
        }
        return task;
    }

    public TaskEventDeleted syncProcess( AggregateObject<String> aggregate, Instant lastModifiedAt, TaskEventDeleted event ) {
        syncDelete( lastModifiedAt, checkSyncDelete( aggregate, event ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventDeleted event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        // Don't know if Deleted, but if not in (Active) then assume deleted
        handlerDelete( checkHandlerDelete( nextVersion, event ), createdAt );
    }

    public TaskEventRestored syncProcess( AggregateObject<String> aggregate, Instant lastModifiedAt, TaskEventRestored event ) {
        syncRestore( lastModifiedAt, checkSyncRestore( aggregate, event ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventRestored event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        // Don't know if Deleted, but if it is then assume it should be restored
        handlerRestore( checkHandlerRestore( nextVersion, event ), createdAt );
    }

    // vvv Testing
    @Override
    protected TaskActive checkHandlerUpdate( long nextVersion, Event<String> event ) {
        return showAsync( event, super.checkHandlerUpdate( nextVersion, event ) );
    }

    @Override
    protected TaskActive checkHandlerDelete( long nextVersion, Event<String> event ) {
        return showAsync( event, super.checkHandlerDelete( nextVersion, event ) );
    }

    @Override
    protected TaskDeleted checkHandlerRestore( long nextVersion, Event<String> event ) {
        return showAsync( event, super.checkHandlerRestore( nextVersion, event ) );
    }

    private <T extends TaskEntity> T showAsync( Event<String> event, T entity ) {
        if ( entity != null ) {
            System.out.println( "************ Async: " + event.getClass().getSimpleName() );
        }
        return entity;
    }
}
