package nl.avthart.todo.app.query.task;

import nl.avthart.todo.app.common.axon.PrimaryProjectionWriteRepository;

public interface TaskPrimaryProjectionWriteRepository extends PrimaryProjectionWriteRepository<String, TaskActive, TaskDeleted>,
                                                              TaskPrimaryProjectionReadRepository {
}
