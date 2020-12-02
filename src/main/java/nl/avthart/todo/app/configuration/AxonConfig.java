package nl.avthart.todo.app.configuration;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.interceptors.EventLoggingInterceptor;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.springboot.util.RegisterDefaultEntities;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings({"unchecked", "rawtypes"})
@Configuration
@RegisterDefaultEntities(packages = "org.axonframework.eventsourcing.eventstore.jpa")
@ComponentScan(basePackages = {
        "org.axonframework.eventsourcing.eventstore.jpa", // DomainEventEntry & SnapshotEventEntry
        "org.axonframework.modelling.saga.repository.jpa", // SagaEntry & AssociationValueEntry
        "org.axonframework.eventhandling.tokenstore.jpa" // TokenEntry
})
public class AxonConfig {

    @Bean
    public CommandBus commandBus() {
        SimpleCommandBus commandBus = SimpleCommandBus.builder().build();
        commandBus.registerDispatchInterceptor( new LoggingInterceptor() );
        return commandBus;
    }

    @Bean
    public QueryBus queryBus() {
        SimpleQueryBus queryBus = SimpleQueryBus.builder().build();
        queryBus.registerDispatchInterceptor( new LoggingInterceptor() );
        return queryBus;
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public EmbeddedEventStore jpaEventStore(
            @Qualifier("jpaEventStorageEngine") EventStorageEngine storageEngine,
            AxonConfiguration configuration
    ) {
        EmbeddedEventStore eventStore = EmbeddedEventStore.builder()
                .storageEngine( storageEngine )
                .messageMonitor( configuration.messageMonitor( EventStore.class, "eventStore" ) )
                .build();
        eventStore.registerDispatchInterceptor( new EventLoggingInterceptor() );
        return eventStore;
    }

    @Bean
    public EventStorageEngine jpaEventStorageEngine(
            EntityManagerProvider entityManagerProvider,
            // EventUpcaster upcasterChain,
            TransactionManager transactionManager
    ) {
        return JpaEventStorageEngine.builder()
                .eventSerializer( JacksonSerializer.builder().build() )
                .entityManagerProvider( entityManagerProvider )
                .transactionManager( transactionManager )
                // .upcasterChain(upcasterChain)
                .build();
    }

//    @Bean
//    public EventUpcaster upcasterChain(List<EventChainFactory> factories) {
//        return new EventUpcasterChain(factories.stream()
//            .map(EventChainFactory::create)
//            .collect(Collectors.toList()));
//    }

    @Bean
    public SagaStore jpaSagaStore( EntityManagerProvider entityManagerProvider ) {
        return JpaSagaStore.builder()
                .entityManagerProvider( entityManagerProvider )
                .build();
    }

    @Bean
    public TokenStore jpaTokenStore( EntityManagerProvider entityManagerProvider ) {
        return JpaTokenStore.builder()
                .entityManagerProvider( entityManagerProvider )
                .serializer( XStreamSerializer.defaultSerializer() )
                .build();
    }
}
