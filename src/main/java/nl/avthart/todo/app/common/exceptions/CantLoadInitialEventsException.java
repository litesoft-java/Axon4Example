package nl.avthart.todo.app.common.exceptions;

@SuppressWarnings("unused")
public class CantLoadInitialEventsException extends RuntimeException {
    public CantLoadInitialEventsException( String message ) {
        super( message );
    }

    public CantLoadInitialEventsException( String message, Throwable cause ) {
        super( message, cause );
    }

    public CantLoadInitialEventsException( Throwable cause ) {
        super( cause );
    }
}
