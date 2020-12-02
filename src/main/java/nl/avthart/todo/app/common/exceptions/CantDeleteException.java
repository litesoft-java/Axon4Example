package nl.avthart.todo.app.common.exceptions;

public class CantDeleteException extends BadRequestException {
    public CantDeleteException( String message, Throwable... cause ) {
        super( message, cause );
    }
}
