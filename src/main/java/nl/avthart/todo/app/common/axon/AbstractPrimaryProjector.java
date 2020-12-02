package nl.avthart.todo.app.common.axon;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import nl.avthart.todo.app.common.exceptions.BusinessRuleException;
import nl.avthart.todo.app.common.exceptions.CantUpdateException;
import nl.avthart.todo.app.common.exceptions.DeletedException;
import nl.avthart.todo.app.common.exceptions.OptimisticLockException;
import nl.avthart.todo.app.common.util.IdSupplier;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RequiredArgsConstructor
public abstract class AbstractPrimaryProjector<ID_Type, EntityActive extends ActiveProjection<ID_Type>, EntityDeleted extends DeletedProjection<ID_Type>> implements PrimaryProjector {
    protected final PrimaryProjectorRepository<ID_Type, EntityActive, EntityDeleted> repo;
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
        EntityActive active = readActive( event );
        if ( (active == null) || (active.getVersion() != (nextVersion - 1)) ) {
            return null;
        }
        return active;
    }

    protected void syncUpdate( Instant lastModifiedAt, EntityActive active ) {
        reportUnexpectedException( dbUpdate( active, () -> repo.save( active, ensureInstant( lastModifiedAt ) ) ) ).run();
    }

    protected void handlerUpdate( EntityActive active, Instant lastModifiedAt ) {
        if ( active != null ) { // Allow Update
            reportAndSwallowException( dbUpdate( active, () -> repo.save( active, ensureInstant( lastModifiedAt ) ) ) ).run();
        }
    }

    // Support (Shared)
    protected void checkSyncUpdateVersion( long currentVersion, EntityActive active ) {
        if ( active.getVersion() != currentVersion ) {
            throw versionError( active );
        }
    }

    protected String error( ID_Type id, String suffix ) {
        return (entityName + " (" + id + ") " + suffix).trim();
    }

    protected String error( IdSupplier<ID_Type> projection, String suffix ) {
        return error( projection.getId(), suffix );
    }

    protected EntityActive readActive( Event<ID_Type> event ) {
        return repo.findActiveById( event.getId() );
    }

    protected EntityDeleted readDeleted( Event<ID_Type> event ) {
        return repo.findDeletedById( event.getId() );
    }

    protected OptimisticLockException versionError( IdSupplier<ID_Type> projection, Throwable... cause ) {
        return new OptimisticLockException( error( projection, "data has changed" ),
                                            cause );
    }

    protected BusinessRuleException constraintError( IdSupplier<ID_Type> projection, String message, Throwable... cause ) {
        return new BusinessRuleException( error( projection, message ),
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
