package nl.avthart.todo.app.query.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface TaskEntryJpaDeletedRepository extends CrudRepository<TaskDeleted, String> {
    Page<TaskDeleted> findByUsernameOrderByLastModifiedDesc( String username, Pageable pageable );
}
