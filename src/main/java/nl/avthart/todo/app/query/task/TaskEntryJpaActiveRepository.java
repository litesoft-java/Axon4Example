package nl.avthart.todo.app.query.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author albert
 */
public interface TaskEntryJpaActiveRepository extends CrudRepository<TaskActive, String> {
    Page<TaskActive> findByUsernameAndCompleted( String username, boolean completed, Pageable pageable );
}
