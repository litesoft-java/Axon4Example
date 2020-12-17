package nl.avthart.todo.app.common.startup;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This version of the RunPostDatabaseReadyRunnables does not guarantee that the
 * PostDatabaseReadyRunnable(s) are run before the SpringBoot Web starts taking web requests!
 */
@Component
public class RunPostDatabaseReadyRunnables {
    private List<PostDatabaseReadyRunnable> runnables;

    public RunPostDatabaseReadyRunnables( List<PostDatabaseReadyRunnable> runnables ) {
        this.runnables = (runnables != null) ? runnables : Collections.emptyList();
    }

    @SuppressWarnings("unused")
    @EventListener
    public void onApplicationEvent( ContextRefreshedEvent event ) {
        execute( "Run", PostDatabaseReadyRunnable::run, true );
    }

    @SuppressWarnings("unused")
    @EventListener
    public void onApplicationEvent( ContextClosedEvent event ) {
        execute( "Shutdown", PostDatabaseReadyRunnable::shutdown, false );
    }

    private void execute( String what, Consumer<PostDatabaseReadyRunnable> method, boolean logExceptions ) {
        if ( !runnables.isEmpty() ) {
            System.out.println( what + ":" );
            for ( PostDatabaseReadyRunnable runnable : runnables ) {
                try {
                    System.out.println( "   " + runnable.getClass().getSimpleName() );
                    method.accept( runnable );
                }
                catch ( Exception e ) {
                    if ( logExceptions ) {
                        e.printStackTrace();
                    } else {
                        throw e;
                    }
                }
            }
        }
    }
}
