package nl.avthart.todo.app.common.util;

public interface IdSupplier<ID_Type> {
    ID_Type getId();

    @SuppressWarnings("unused")
    static <ID_Type> ID_Type resolve( IdSupplier<ID_Type> idSupplier) {
        return (idSupplier == null) ? null : idSupplier.getId();
    }
}