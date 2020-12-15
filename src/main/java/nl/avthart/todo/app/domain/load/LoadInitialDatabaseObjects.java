package nl.avthart.todo.app.domain.load;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nl.avthart.todo.app.common.axon.PrimaryProjector;
import nl.avthart.todo.app.common.exceptions.CantLoadInitialEventsException;
import nl.avthart.todo.app.common.startup.PostDatabaseReadyRunnable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadInitialDatabaseObjects implements PostDatabaseReadyRunnable {
    private final ReadLoadYaml mapYaml;
    private final Loader loader;
    private final List<PrimaryProjector> primaryProjectors;

    @Override
    public void run() {
        //    Object id = commandGateway.sendAndWait( TaskCommandLoad.builder().title( "Freddy" ).username( "foo" ).build() );
        //    commandGateway.sendAndWait( new TaskCommandDelete( id.toString() ) );

        for ( PrimaryProjector projector : primaryProjectors ) {
            projector.ensureProjectionsCurrent();
        }

        ClassPathResource resource = new ClassPathResource( "initialLoad.yml" );

        Path path = null;
        List<String> lines = null;
        try {
            File file = resource.getFile();
            path = file.toPath();
            lines = Files.readAllLines( path );
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }

        if ( lines == null ) {
            return;
        }
        Map<String, List<Map<String, ?>>> map = mapYaml.read( path.toString(), lines );
        Loader.Result result = loader.load( map );
        List<String> errors = result.getErrors();
        if ( !errors.isEmpty() ) {
            throw new CantLoadInitialEventsException( String.join( "\n", errors ) );
        }

        System.out.println( "LoadInitialDatabaseObjects:\n" + result );
    }
}
