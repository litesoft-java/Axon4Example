package nl.avthart.todo.app.common.startup;

public interface PostDatabaseReadyRunnable extends Runnable {
    default void shutdown() {
    }
}
