package nl.avthart.todo.app.common.axon;

import nl.avthart.todo.app.common.util.IdSupplier;

public interface AggregateObject<ID_Type> extends IdSupplier<ID_Type> {
    long getVersion();

    boolean isDeleted();
}
