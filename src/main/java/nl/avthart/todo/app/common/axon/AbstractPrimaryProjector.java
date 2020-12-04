package nl.avthart.todo.app.common.axon;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
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
public abstract class AbstractPrimaryProjector<ID_Type, LoadCommand extends IdSupplier<ID_Type>, EntityActive extends ActiveProjection<ID_Type>, EntityDeleted extends DeletedProjection<ID_Type>> implements PrimaryProjector {
    protected final PrimaryProjectorRepository<ID_Type, EntityActive, EntityDeleted> repo;
    private final CommandGateway commandGateway;
    private final String entityName;

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
    protected EntityActive checkSyncUpdate( long currentVersion, Event<ID_Type> event ) {
        EntityActive active = readActive( event );
        if ( active == null ) {
            boolean deleted = (null != readDeleted( event ));
            if ( deleted ) {
                throw new DeletedException( error( event, "is currently deleted" ) );
            }
            throw new CantUpdateException( error( event, "does not currently exist" ) );
        }
        checkSyncUpdateVersion( currentVersion, active );
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
    protected EntityActive checkSyncDelete( long currentVersion, Event<ID_Type> event ) {
        EntityActive active = readActive( event );
        if ( active == null ) {
            boolean deleted = (null != readDeleted( event ));
            if ( deleted ) {
                throw new DeletedException( error( event, "is already deleted" ) );
            }
            throw new CantDeleteException( error( event, "does not currently exist" ) );
        }
        checkSyncUpdateVersion( currentVersion, active );
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
    protected EntityDeleted checkSyncRestore( long currentVersion, Event<ID_Type> event ) {
        EntityDeleted deleted = readDeleted( event );
        if ( deleted == null ) {
            boolean active = (null != readActive( event ));
            throw new CantRestoreException( error( event, active ?
                                                          "is currently active" :
                                                          "does not currently exist" ) );
        }
        checkSyncUpdateVersion( currentVersion, deleted );
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
        EntityActive active = readActive( command );
        Object nextCommand = (active == null) ?
                             createCreateEvent( command ) :
                             optionalUpdate( active, command, overwrite );
        return (nextCommand == null) ? null : commandGateway.sendAndWait( nextCommand );
    }

    protected abstract Object createCreateEvent( LoadCommand command );

    protected abstract Object optionalUpdate( EntityActive active, LoadCommand command, boolean overwrite );

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

    private Runnable reportUnexpectedException( Runnable proxy ) {
        return () -> {
            try {
                proxy.run();
            }
            catch ( OptimisticLockException | BusinessRuleException e ) { // Expected
                throw e;
            }
            catch ( RuntimeException e ) { // Unexpected
                e.printStackTrace(); // report
                throw e;
            }
        };
    }

    private Runnable reportAndSwallowException( Runnable proxy ) {
        return () -> {
            try {
                proxy.run();
            }
            catch ( RuntimeException e ) {
                e.printStackTrace(); // Report
                // Swallow
            }
        };
    }

    private Runnable dbInsert( Projection<ID_Type> projection, Runnable repoCall ) {
        return () -> dbChange( projection, repoCall ).run();
    }

    private Runnable dbUpdate( Projection<ID_Type> projection, Runnable repoCall ) {
        return () -> {
            try {
                dbChange( projection, repoCall ).run();
            }
            catch ( ObjectOptimisticLockingFailureException | StaleObjectStateException | javax.persistence.OptimisticLockException e ) {
                throw versionError( projection, e );
            }
        };
    }

    private Runnable dbChange( Projection<ID_Type> projection, Runnable repoCall ) {
        return () -> {
            try {
                repoCall.run();
            }
            catch ( DataIntegrityViolationException | ConstraintViolationException e ) {
                throw constraintError( projection, e.getMessage(), e );
            }
        };
    }
}
