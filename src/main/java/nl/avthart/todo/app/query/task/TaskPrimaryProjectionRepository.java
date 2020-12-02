package nl.avthart.todo.app.query.task;

import nl.avthart.todo.app.common.axon.PrimaryProjectorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskPrimaryProjectionRepository extends PrimaryProjectorRepository<String, TaskActive, TaskDeleted> {
    Page<TaskActive> findActiveByUsernameAndCompleted( String username, boolean completed, Pageable pageable );

    Page<TaskDeleted> findDeletedByUsername( String username, Pageable pageable );
}
