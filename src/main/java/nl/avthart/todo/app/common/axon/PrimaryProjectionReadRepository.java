package nl.avthart.todo.app.common.axon;

public interface PrimaryProjectionReadRepository<ID_Type, EntityActive extends ActiveProjection<ID_Type>, EntityDeleted extends DeletedProjection<ID_Type>> {
    EntityActive findActiveById( ID_Type id );

    EntityDeleted findDeletedById( ID_Type id );
}
