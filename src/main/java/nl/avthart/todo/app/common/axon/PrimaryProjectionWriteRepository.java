package nl.avthart.todo.app.common.axon;

import java.time.Instant;

public interface PrimaryProjectionWriteRepository<ID_Type, EntityActive extends ActiveProjection<ID_Type>, EntityDeleted extends DeletedProjection<ID_Type>>
        extends PrimaryProjectionReadRepository<ID_Type, EntityActive, EntityDeleted> {

    void save( EntityActive active, Instant lastModifiedAt );

    void delete( EntityActive active, Instant lastModifiedAt );

    void restore( EntityDeleted deleted, Instant lastModifiedAt );
}
