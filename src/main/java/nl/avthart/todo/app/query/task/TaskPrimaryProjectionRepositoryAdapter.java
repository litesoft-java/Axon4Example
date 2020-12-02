package nl.avthart.todo.app.query.task;

import java.time.Instant;
import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskPrimaryProjectionRepositoryAdapter implements TaskPrimaryProjectionRepository {
    private final TaskEntryJpaActiveRepository activeRepo;
    private final TaskEntryJpaDeletedRepository deletedRepo;

    @Override
    public Page<TaskActive> findActiveByUsernameAndCompleted( String username, boolean completed, Pageable pageable ) {
        return activeRepo.findByUsernameAndCompleted( username, completed, pageable );
    }

    @Override
    public Page<TaskDeleted> findDeletedByUsername( String username, Pageable pageable ) {
        return deletedRepo.findByUsernameOrderByLastModifiedDesc( username, pageable );
    }

    @Override
    public TaskActive findActiveById( String id ) {
        return activeRepo.findById( id ).orElse( null );
    }

    @Override
    public TaskDeleted findDeletedById( String id ) {
        return deletedRepo.findById( id ).orElse( null );
    }

    @Override
    public void save( TaskActive active, Instant lastModifiedAt ) {
        active.setLastModified( lastModifiedAt );
        activeRepo.save( active );
    }

    @Override
    @Transactional
    public void delete( TaskActive active, Instant lastModifiedAt ) {
        TaskDeleted deleted = new TaskDeleted( update( active, lastModifiedAt ) ); // Create decoupled Delete (from coupled Active)
        deletedRepo.save( deleted );
        activeRepo.delete( active );
    }

    @Override
    @Transactional
    public void restore( TaskDeleted deleted, Instant lastModifiedAt ) {
        TaskActive active = new TaskActive( update( deleted, lastModifiedAt ) ); // Create decoupled Active (from coupled Delete)
        activeRepo.save( active );
        deletedRepo.delete( deleted );
    }

    private static TaskEntity update( TaskEntity entity, Instant lastModifiedAt ) {
        return entity.toBuilder().version( entity.version + 1 ).lastModified( lastModifiedAt ).build();
    }
}
