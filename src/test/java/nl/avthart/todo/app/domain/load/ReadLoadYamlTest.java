package nl.avthart.todo.app.domain.load;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ReadLoadYamlTest {
    private static final ReadLoadYaml LOADER = new ReadLoadYaml();

    @Test
    void readOk() {
        Map<String, List<Map<String, ?>>> result = LOADER.read(
                "Test",
                "",
                "Task:",
                "  - username: Mikaela",
                "    createdHour: 2011-01-16T12Z",
                "    title: Born",
                "    starred: true",
                "  - username: Devin",
                "    createdHour: 1981-01-15T12Z"
        );
        try {
            assertEquals( 1, result.size() );
            List<Map<String, ?>> tasks = result.get( "Task" );
            assertEquals( 2, tasks.size() );
            checkTask(tasks.get(0), "Mikaela", "2011-01-16T12Z", "Born", true);
            checkTask(tasks.get(1), "Devin", "1981-01-15T12Z", null, null);
        }
        catch ( RuntimeException e ) {
            System.out.println( "ReadLoadYamlTest.readOk:\n" + result );
            throw e;
        }
    }

    private void checkTask( Map<String, ?> task, String username, String createdHour, String title, Boolean starred ) {
        assertEquals( username, task.get( "username" ) );
        assertEquals( createdHour, task.get( "createdHour" ) );
        assertEquals( title, task.get( "title" ) );
        assertEquals( starred, task.get( "starred" ) );
    }

    @Test
    void readFailNotList() {
        checkFailed( "not a list","",
                "Task: Fail"
        );
    }

    @Test
    void readFailNotMap() {
        checkFailed( "not a map","",
                "Task:",
                " - true"
        );
    }

    @Test
    void readFailNumberKey() {
        checkFailed( "start with a letter or underscore","",
                "Task:",
                " - 22: Mikaela"
        );
    }

    @Test
    void readFailContainsSpaces() {
        checkFailed( "contains a space","",
                "Task:",
                " - Ш Щ Ь Й Ы Я Г И : Mikaela"
        );
    }

    private void checkFailed( String failureText, String... lines ) {
        try {
            Object result = LOADER.read( "Test", lines );
            fail( "Expected failure with '" + failureText + "', but succeeded with result: " + result );
        }
        catch ( ReadLoadYaml.ParsingException e ) {
            if ( e.getMessage().contains( failureText ) ) {
                return;
            }
            fail( "Expected failure with '" + failureText + "', but failed with: " + e );
        }
    }
}