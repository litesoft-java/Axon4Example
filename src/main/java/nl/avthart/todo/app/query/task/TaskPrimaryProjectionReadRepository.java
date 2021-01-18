package nl.avthart.todo.app.query.task;

import nl.avthart.todo.app.common.axon.PrimaryProjectionReadRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskPrimaryProjectionReadRepository extends PrimaryProjectionReadRepository<String, TaskActive, TaskDeleted> {
    Page<TaskActive> findActiveByUsernameAndCompleted( String username, boolean completed, Pageable pageable );

    Page<TaskDeleted> findDeletedByUsername( String username, Pageable pageable );
}
