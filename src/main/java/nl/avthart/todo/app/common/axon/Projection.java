package nl.avthart.todo.app.common.axon;

import java.time.Instant;

import nl.avthart.todo.app.common.util.IdSupplier;

@SuppressWarnings("unused")
public interface Projection<ID_Type> extends IdSupplier<ID_Type> {
    void setId( ID_Type id );

    Long getVersion();

    void setVersion( Long version );

    Instant getLastModified();

    void setLastModified( Instant lastModified );
}
