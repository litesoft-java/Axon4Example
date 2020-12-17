package nl.avthart.todo.app.flags;

import java.time.Instant;

import sun.jvmstat.monitor.MonitorException;

public class Monitor {
    private static boolean report = false;

    public static void show() {
        if ( report ) {
            new MonitorException( Instant.now().toString() ).printStackTrace();
        }
    }

    public static void activate() {
        report = true;
    }
}
