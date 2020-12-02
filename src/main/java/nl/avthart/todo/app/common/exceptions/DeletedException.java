package nl.avthart.todo.app.common.exceptions;

public class DeletedException extends BadRequestException {
    public DeletedException( String message, Throwable... cause ) {
        super( message, cause );
    }
}
