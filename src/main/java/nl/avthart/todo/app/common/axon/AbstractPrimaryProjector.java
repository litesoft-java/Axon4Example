package nl.avthart.todo.app.common.axon;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import nl.avthart.todo.app.common.exceptions.AbstractCommonException;
import nl.avthart.todo.app.common.exceptions.BusinessRuleException;
import nl.avthart.todo.app.common.exceptions.CantDeleteException;
import nl.avthart.todo.app.common.exceptions.CantRestoreException;
import nl.avthart.todo.app.common.exceptions.CantUpdateException;
import nl.avthart.todo.app.common.exceptions.DeletedException;
import nl.avthart.todo.app.common.exceptions.OptimisticLockException;
import nl.avthart.todo.app.common.util.IdSupplier;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RequiredArgsConstructor
public abstract class AbstractPrimaryProjector<ID_Type, Command extends IdSupplier<ID_Type>, LoadCommand extends IdSupplier<ID_Type>, EntityActive extends ActiveProjection<ID_Type>, EntityDeleted extends DeletedProjection<ID_Type>> implements PrimaryProjector {
    protected final PrimaryProjectionWriteRepository<ID_Type, EntityActive, EntityDeleted> repo;
    private final LastEventReader lastEventReader;
    private final CommandGateway commandGateway;
    private final String entityName;
    private final Class<? extends AggregateObject<ID_Type>> aggregateClass;

    public void ensureProjectionsCurrent() {
        LastEvent event = lastEventReader.read( aggregateClass );
        if ( event != null ) {
            ID_Type id = fromLastEventAggregateId( event.getAggregateIdentifier() );
            boolean deleted = event.getPayloadType().endsWith( "Deleted" ); // Requires all Deleted Events to end with Deleted
            for ( int i = 0; !idExists( deleted, id ); i++ ) {
                try {
                    Thread.sleep( 250 );
                }
                catch ( InterruptedException notReallyExpectedBut ) {
                    notReallyExpectedBut.printStackTrace();
                }
                if ( (i & 7) == 0 ) { // Every 8th loop (2secs)
                    System.out.println( "Waiting on Projector to finish last event projection for: " + entityName );
                }
            }
        }
    }

    private boolean idExists( boolean deleted, ID_Type id ) {
        return (null != (deleted ?
                         repo.findDeletedById( id ) :
                         repo.findActiveById( id )));
    }

    abstract protected ID_Type fromLastEventAggregateId( String id );

    protected Instant ensureInstant( Instant instant ) {
        return (instant != null) ? instant : Instant.now();
    }

    // Create Support
    protected void syncCreate( Instant createdAt, Event<ID_Type> event, EntityActive active ) {
        if ( null != readActive( event ) ) {
            throw constraintError( active, "already exists" );
        }
        reportUnexpectedException( dbInsert( active, () -> repo.save( active, createdAt ) ) ).run();
    }

    protected void handlerCreate( Event<ID_Type> event, Instant createdAt, EntityActive active ) {
        if ( null == readActive( event ) ) {
            reportAndSwallowException( dbInsert( active, () -> repo.save( active, createdAt ) ) ).run();
        }
    }

    // Update Support
    protected EntityActive checkSyncUpdate( AggregateObject<ID_Type> aggregate, Event<ID_Type> event ) {
        if ( aggregate.isDeleted() ) {
            throw new DeletedException( error( event, "is currently deleted" ) );
        }
        EntityActive active = readActive( event );
        if ( active == null ) {
            throw new CantUpdateException( error( event, "does not currently exist" ) );
        }
        checkSyncUpdateVersion( aggregate.getVersion(), active );
        return active;
    }

    protected EntityActive checkHandlerUpdate( long nextVersion, Event<ID_Type> event ) {
        return checkHandlerActiveVersion( nextVersion, event );
    }

    protected void syncUpdate( Instant lastModifiedAt, EntityActive active ) {
        reportUnexpectedException( dbUpdate( active, () -> repo.save( active, ensureInstant( lastModifiedAt ) ) ) ).run();
    }

    protected void handlerUpdate( EntityActive active, Instant lastModifiedAt ) {
        if ( active != null ) { // Allow Update
            reportAndSwallowException( dbUpdate( active, () -> repo.save( active, ensureInstant( lastModifiedAt ) ) ) ).run();
        }
    }

    // Delete Support
    protected EntityActive checkSyncDelete( AggregateObject<ID_Type> aggregate, Event<ID_Type> event ) {
        if ( aggregate.isDeleted() ) {
            throw new DeletedException( error( event, "is already deleted" ) );
        }
        EntityActive active = readActive( event );
        if ( active == null ) {
            throw new CantDeleteException( error( event, "does not currently exist" ) );
        }
        checkSyncUpdateVersion( aggregate.getVersion(), active );
        return active;
    }

    protected EntityActive checkHandlerDelete( long nextVersion, Event<ID_Type> event ) {
        return checkHandlerActiveVersion( nextVersion, event );
    }

    protected void syncDelete( Instant lastModifiedAt, EntityActive active ) {
        reportUnexpectedException( dbUpdate( active, () -> repo.delete( active, ensureInstant( lastModifiedAt ) ) ) ).run();
    }

    protected void handlerDelete( EntityActive active, Instant lastModifiedAt ) {
        if ( active != null ) { // Allow Delete
            reportAndSwallowException( dbUpdate( active, () -> repo.delete( active, ensureInstant( lastModifiedAt ) ) ) ).run();
        }
    }

    // Restore Support
    protected EntityDeleted checkSyncRestore( AggregateObject<ID_Type> aggregate, Event<ID_Type> event ) {
        if ( !aggregate.isDeleted() ) { // Active!
            throw new CantRestoreException( error( event, "is currently active" ) );
        }
        EntityDeleted deleted = readDeleted( event );
        if ( deleted == null ) {
            throw new CantRestoreException( error( event, "does not currently exist" ) );
        }
        checkSyncUpdateVersion( aggregate.getVersion(), deleted );
        return deleted;
    }

    protected EntityDeleted checkHandlerRestore( long nextVersion, Event<ID_Type> event ) {
        EntityDeleted deleted = readDeleted( event );
        if ( (deleted == null) || (deleted.getVersion() != (nextVersion - 1)) ) {
            return null;
        }
        return deleted;
    }

    protected void syncRestore( Instant lastModifiedAt, EntityDeleted delete ) {
        reportUnexpectedException( dbUpdate( delete, () -> repo.restore( delete, ensureInstant( lastModifiedAt ) ) ) ).run();
    }

    protected void handlerRestore( EntityDeleted delete, Instant lastModifiedAt ) {
        if ( delete != null ) { // Allow Restore
            reportAndSwallowException( dbUpdate( delete, () -> repo.restore( delete, ensureInstant( lastModifiedAt ) ) ) ).run();
        }
    }

    // Support (Shared)
    protected Object load( LoadCommand command, boolean overwrite ) {
        if ( command == null ) {
            throw new NullPointerException( "No LoadCommand" );
        }
        EntityActive active = commandReadActive( command );
        Command nextCommand = (active == null) ?
                              createCreateCommand( command ) :
                              optionalUpdateCommand( active, command, overwrite );
        if ( nextCommand == null ) {
            return null;
        }
        Object rv = commandGateway.sendAndWait( nextCommand );
        return (rv != null) ? rv : command.getId();
    }

    protected abstract Command createCreateCommand( LoadCommand command );

    protected abstract Command optionalUpdateCommand( EntityActive active, LoadCommand command, boolean overwrite );

    protected EntityActive commandReadActive( LoadCommand command ) {
        return readActive( command );
    }

    private EntityActive checkHandlerActiveVersion( long nextVersion, Event<ID_Type> event ) {
        EntityActive active = readActive( event );
        if ( (active == null) || (active.getVersion() != (nextVersion - 1)) ) {
            return null;
        }
        return active;
    }

    protected void checkSyncUpdateVersion( long currentVersion, Projection<ID_Type> projection ) {
        if ( projection.getVersion() != currentVersion ) {
            throw versionError( projection );
        }
    }

    protected String error( ID_Type id, String suffix ) {
        return (entityName + " (" + id + ") " + suffix).trim();
    }

    protected String error( IdSupplier<ID_Type> idSupplier, String suffix ) {
        return error( IdSupplier.resolve( idSupplier ), suffix );
    }

    protected EntityActive readActive( IdSupplier<ID_Type> idSupplier ) {
        ID_Type id = IdSupplier.resolve( idSupplier );
        return (id == null) ? null : repo.findActiveById( id );
    }

    protected EntityDeleted readDeleted( IdSupplier<ID_Type> idSupplier ) {
        ID_Type id = IdSupplier.resolve( idSupplier );
        return (id == null) ? null : repo.findDeletedById( id );
    }

    protected OptimisticLockException versionError( IdSupplier<ID_Type> idSupplier, Throwable... cause ) {
        return new OptimisticLockException( error( idSupplier, "data has changed" ),
                                            cause );
    }

    protected BusinessRuleException constraintError( IdSupplier<ID_Type> idSupplier, String message, Throwable... cause ) {
        return new BusinessRuleException( error( idSupplier, message ),
                                          cause );
    }

    private interface ExceptionalRunnable {
        void run()
                throws Exception;
    }

    private Runnable reportAndSwallowException( ExceptionalRunnable proxy ) {
        return () -> {
            try {
                proxy.run();
            }
            catch ( Exception e ) {
                e.printStackTrace(); // Report
                // Swallow
            }
        };
    }

    private Runnable reportUnexpectedException( ExceptionalRunnable proxy ) {
        return () -> {
            try {
                proxy.run();
            }
            catch ( AbstractCommonException e ) { // Expected
                throw e;
            }
            catch ( Exception e ) { // Unexpected
                throw map( e );
            }
        };
    }

    private RuntimeException map( Exception e ) {
        AbstractCommonException ace = AbstractCommonException.map( e );
        if ( ace != null ) {
            return ace;
        }
        e.printStackTrace(); // report
        return (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException( e );
    }

    private ExceptionalRunnable dbInsert( Projection<ID_Type> projection, ExceptionalRunnable repoCall ) {
        return () -> dbChange( projection, repoCall ).run();
    }

    private ExceptionalRunnable dbUpdate( Projection<ID_Type> projection, ExceptionalRunnable repoCall ) {
        return () -> {
            try {
                dbChange( projection, repoCall ).run();
            }
            catch ( ObjectOptimisticLockingFailureException | StaleObjectStateException | javax.persistence.OptimisticLockException e ) {
                throw versionError( projection, e );
            }
        };
    }

    private ExceptionalRunnable dbChange( Projection<ID_Type> projection, ExceptionalRunnable repoCall ) {
        return () -> {
            try {
                repoCall.run();
            }
            catch ( SQLIntegrityConstraintViolationException | DataIntegrityViolationException | ConstraintViolationException e ) {
                throw constraintError( projection, "SQL Constraint Violation", e );
            }
        };
    }
}
