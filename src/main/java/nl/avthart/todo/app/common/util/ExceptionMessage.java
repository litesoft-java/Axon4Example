package nl.avthart.todo.app.common.util;

import java.util.Arrays;
import java.util.List;

import nl.avthart.todo.app.common.exceptions.AbstractCommonException;

public class ExceptionMessage {
    private static final List<String> IGNORE_EXCEPTION_SIMPLE_CLASS_NAMES = Arrays.asList(
            "UndeclaredThrowableException",
            "InvocationTargetException"
    );

    private final StringBuilder sb = new StringBuilder();

    private ExceptionMessage() {
    }

    public static String from( Throwable t ) {
        if ( t == null ) {
            return "";
        }
        ExceptionMessage collector = new ExceptionMessage();
        collector.add( t );
        return collector.sb.toString();
    }

    private void add( Throwable t ) {
        while ( true ) {
            Throwable cause = t.getCause();
            if ( cause == t ) {
                cause = null;
            }
            String simpleName = t.getClass().getSimpleName();
            if ( (cause != null) && IGNORE_EXCEPTION_SIMPLE_CLASS_NAMES.contains( simpleName ) ) {
                t = cause;
                continue;
            }
            String message = clean( t.getMessage() );
            sb.append( (message != null) ? message : simpleName );
            if ( (cause == null) || (t instanceof AbstractCommonException) ) {
                return;
            }
            sb.append( "\ncaused by: " );
            t = cause;
        }
    }

    private static String clean( String s ) {
        if ( s != null ) {
            s = s.trim();
            if ( s.isEmpty() ) {
                s = null;
            }
        }
        return s;
    }
}
