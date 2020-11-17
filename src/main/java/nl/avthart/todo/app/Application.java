package nl.avthart.todo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Todo App using Axon and Spring Boot
 *
 * @author albert
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "org.axonframework.eventsourcing.eventstore.jpa", // DomainEventEntry & SnapshotEventEntry
        "org.axonframework.modelling.saga.repository.jpa", // SagaEntry & AssociationValueEntry
        "org.axonframework.eventhandling.tokenstore.jpa", // TokenEntry
        "nl.avthart.todo.app"})
public class Application {

    public static void main( String[] args ) {
        SpringApplication.run( Application.class, args );
    }
}
