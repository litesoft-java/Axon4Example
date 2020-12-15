package nl.avthart.todo.app.common.axon;

public interface LastEventReader {
    LastEvent read(Class<? extends AggregateObject> aggregateType);
}
