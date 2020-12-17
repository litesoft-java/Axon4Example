package nl.avthart.todo.app.common.exceptions;

public abstract class AbstractCommonException extends RuntimeException {
    protected AbstractCommonException( String message ) {
        super( message );
    }

    protected AbstractCommonException( String message, Throwable cause ) {
        super( message, cause );
    }

    protected AbstractCommonException( Throwable cause ) {
        super( cause );
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractCommonException> T map( Throwable cause ) {
        for ( Throwable current = cause; current != null; current = cause ) {
            if (current instanceof AbstractCommonException) {
                return (T)current;
            }
            cause = current.getCause();
            if (cause == current) {
                cause = null;
            }
        }
        return null;
    }
}
