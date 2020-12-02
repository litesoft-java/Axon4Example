package nl.avthart.todo.app.common.exceptions;

public class CantUpdateException extends BadRequestException {
    public CantUpdateException( String message, Throwable... cause ) {
        super( message, cause );
    }
}
