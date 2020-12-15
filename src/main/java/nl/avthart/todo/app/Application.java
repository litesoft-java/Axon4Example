package nl.avthart.todo.app;

import java.util.ArrayList;
import java.util.List;

import nl.avthart.todo.app.common.axon.LastEventSqlSupport;
import nl.avthart.todo.app.flags.Monitor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Todo App using Axon and Spring Boot
 *
 * @author albert
 */
@SpringBootApplication
public class Application {

    public static void main( String[] args ) {
        if ( args != null ) {
            List<String> others = new ArrayList<>();
            for ( String arg : args ) {
                if ( "monitor".equalsIgnoreCase( arg ) ) {
                    Monitor.activate();
                } else {
                    others.add( arg );
                }
            }
            args = others.toArray( new String[0] );
        }
        SpringApplication.run( Application.class, args );
    }

    @Bean
    LastEventSqlSupport lastEventSqlSupport() {
        return LastEventSqlSupport.builder().tableName( "DOMAIN_EVENT_ENTRY" )
                .aggregateType( "TYPE" )
                .globalIndex( "GLOBAL_INDEX" )
                .aggregateIdentifier( "AGGREGATE_IDENTIFIER" )
                .sequenceNumber( "SEQUENCE_NUMBER" )
                .payloadType( "PAYLOAD_TYPE" )
                .build();
    }
}
