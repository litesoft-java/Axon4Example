package nl.avthart.todo.app.common.axon;

import java.time.Instant;

public interface PrimaryProjectorRepository<ID_Type, EntityActive extends ActiveProjection<ID_Type>, EntityDeleted extends DeletedProjection<ID_Type>> {
    EntityActive findActiveById( ID_Type id );

    EntityDeleted findDeletedById( ID_Type id );

    void save( EntityActive active, Instant lastModifiedAt );

    void delete( EntityActive active, Instant lastModifiedAt );

    void restore( EntityDeleted deleted, Instant lastModifiedAt );
}
