package nl.avthart.todo.app.common.exceptions;

public class CantRestoreException extends BadRequestException {
    public CantRestoreException( String message, Throwable... cause ) {
        super( message, cause );
    }
}
