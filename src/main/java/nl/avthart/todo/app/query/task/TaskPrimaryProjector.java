package nl.avthart.todo.app.query.task;

import java.time.Instant;

import nl.avthart.todo.app.common.axon.AbstractPrimaryProjector;
import nl.avthart.todo.app.common.axon.Event;
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
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.stereotype.Component;

@Component
public class TaskPrimaryProjector extends AbstractPrimaryProjector<String, TaskCommand, AbstractTaskCommandLoad, TaskActive, TaskDeleted> {

    public TaskPrimaryProjector( TaskPrimaryProjectionRepository repo, CommandGateway commandGateway ) {
        super( repo, commandGateway, "Task" );
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
        command.setCreatedHour( loadUpdateField( active.getCreatedHour(), command.getCreatedHour() ) );
        command.setUsername( loadUpdateField( active.getUsername(), command.getUsername() ) );
        command.setTitle( loadUpdateField( active.getTitle(), command.getTitle() ) );
        if ( active.isEquivalent( command ) ) {
            return null; // No change needed
        }
        if ( !overwrite ) {
            throw new CantUpdateException( error( active, active.delta( command ) ) );
        }
        return new TaskCommandUpdate( active.getId(),
                                      command.getCreatedHour(),
                                      command.getUsername(),
                                      command.getTitle(),
                                      command.isCompleted(),
                                      command.isStarred() );
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
        return new TaskActive( TaskEntity.builder()
                                       .id( event.getId() )
                                       .version( 0L )
                                       .createdHour( loadUpdateField( event.getCreatedHour(), toHour( createdAt ) ) )
                                       .username( event.getUsername() )
                                       .title( event.getTitle() )
                                       .completed( event.isCompleted() )
                                       .starred( event.isStarred() )
                                       .build() );
    }

    // Updated
    public TaskEventUpdated syncProcess( long currentVersion, Instant lastModifiedAt, TaskEventUpdated event ) {
        syncUpdate( lastModifiedAt, map( event, checkSyncUpdate( currentVersion, event ) ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventUpdated event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        handlerUpdate( map( event, checkHandlerUpdate( nextVersion, event ) ), createdAt );
    }

    private TaskActive map( TaskEventUpdated event, TaskActive task ) {
        if ( task != null ) {
            task.setCreatedHour( loadUpdateField( task.getCreatedHour(), event.getCreatedHour() ) );
            task.setUsername( event.getUsername() );
            task.setTitle( event.getTitle() );
            task.setCompleted( event.isCompleted() );
            task.setStarred( event.isStarred() );
        }
        return task;
    }

    // Starred
    public TaskEventStarred syncProcess( long currentVersion, Instant lastModifiedAt, TaskEventStarred event ) {
        syncUpdate( lastModifiedAt, map( event, checkSyncUpdate( currentVersion, event ) ) );
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

    public TaskEventUnstarred syncProcess( long currentVersion, Instant lastModifiedAt, TaskEventUnstarred event ) {
        TaskActive task = checkSyncUpdate( currentVersion, event );
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

    public TaskEventTitleModified syncProcess( long currentVersion, Instant lastModifiedAt, TaskEventTitleModified event ) {
        TaskActive task = checkSyncUpdate( currentVersion, event );
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

    public TaskEventCompleted syncProcess( long currentVersion, Instant lastModifiedAt, TaskEventCompleted event ) {
        syncUpdate( lastModifiedAt, map( event, checkSyncUpdate( currentVersion, event ) ) );
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

    public TaskEventDeleted syncProcess( long currentVersion, Instant lastModifiedAt, TaskEventDeleted event ) {
        // Not currently Deleted (checked in Aggregate)
        syncDelete( lastModifiedAt, checkSyncDelete( currentVersion, event ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventDeleted event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        // Don't know if Deleted, but if not in (Active) then assume deleted
        handlerDelete( checkHandlerDelete( nextVersion, event ), createdAt );
    }

    public TaskEventRestored syncProcess( long currentVersion, Instant lastModifiedAt, TaskEventRestored event ) {
        // Currently Deleted (checked in Aggregate)
        syncRestore( lastModifiedAt, checkSyncRestore( currentVersion, event ) );
        return event;
    }

    @SuppressWarnings("unused")
    @EventHandler
    void on( TaskEventRestored event, @SequenceNumber long nextVersion, @Timestamp Instant createdAt ) {
        // Don't know if Deleted, but if it is then assume it should be restored
        handlerRestore( checkHandlerRestore( nextVersion, event ), createdAt );
    }

    private String toHour( Instant instant ) {
        // 2020-11-19T13Z
        // 01234567-10123
        // 2020-11-19T13:12:11.101Z
        return instant.toString().substring( 0, 13 ) + "Z";
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
