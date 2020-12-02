package nl.avthart.todo.app.common.exceptions;

import java.util.ArrayList;
import java.util.List;

public abstract class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1518440584190922771L;

    protected BadRequestException( String message, Throwable... cause ) {
        super( message, cause( cause ) );
    }

    public static Throwable cause( Throwable... causes ) {
        if ( (causes == null) || (causes.length == 0) ) {
            return null;
        }
        if ( causes.length == 1 ) {
            return causes[0];
        }
        List<Throwable> nonNulls = new ArrayList<>();
        for ( Throwable cause : causes ) {
            if ( cause != null ) {
                nonNulls.add( cause );
            }
        }
        if ( nonNulls.isEmpty() ) {
            return null;
        }
        Throwable nonNulls1 = nonNulls.remove( 0 );
        if ( nonNulls.isEmpty() ) {
            return nonNulls1;
        }
        StringBuilder sb = new StringBuilder().append( nonNulls1.getMessage() );
        for ( Throwable cause : nonNulls ) {
            sb.append( "\n" ).append( cause.getMessage() );
        }
        return new CompoundException( sb.toString(), nonNulls1 );
    }

    public static class CompoundException extends Exception {
        public CompoundException( String message, Throwable cause ) {
            super( message, cause );
        }
    }
}
