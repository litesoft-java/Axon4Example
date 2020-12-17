package nl.avthart.todo.app.common.exceptions;

public class OptimisticLockException extends BadRequestException {
    public OptimisticLockException( String message, Throwable... cause ) {
        super( message, cause );
    }
}
