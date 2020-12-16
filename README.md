# Spring Boot Axon Sample

## Introduction

This is a sample application to demonstrate Spring Boot and Axon Framework.

The original source was Spring Boot (1.5.x) and Axon Framework (3.x) using Synchronous Projections, and was sourced from (thanks to, Albert van 't Hart):

    https://github.com/avthart/spring-boot-axon-sample

The code has been updated to use Spring Boot (2.3.4) and Axon Framework (4.3).  Different branches use different techniques to manage the Projections.

The Todo application makes use of the following design patterns:
- Domain Driven Design
- CQRS
- Event Sourcing
- Task based User Interface

## Building

> mvn package

## Running

> mvn spring-boot:run

Home page, browse to   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  http://localhost:8080/index.html

Embedded H2, browse to                         &nbsp;&nbsp;  http://localhost:8080/h2-console

## Concepts

- One of the key features of Axon is the "EventProcessor"s that are leveraged to drive classes
        that have "@EventHandler" methods that maintain the projections (normally stored in a DB)
        that will be used as the query sources.  They come in two flavors "TrackingEventProcessor"
        (synchronous - running in a background thread) and "SubscribingEventProcessor"
        (asynchronous - running in the primary thread - same one that store the Event).  The classes
        that have "@EventHandler" methods that maintain the projections in this code are called:
            "Projector"s.

## Observations

- The Token Tracking infrastructure used by the "TrackingEventProcessor"s uses an Owner model with a
        moving Timestamp to facilitate multiple instances of the claiming the processing of a particular
        Projector (note: Projectors are registered by package, so you can only have one projector per package).   

- A Projector that fails (throwing an exception) to processes an Event will attempt to process that Event over
        and over!  This effectively means that an "@EventHandler" method **should** not be allowed to fail!  

## Goals

1. Ensure that Events are not created/stored when the Event would create an invalid state
          that is related to global (across records) constraints 
          (classic RDB unique secondary indexes that enforce business uniqueness rules).

2. Leverage certain Axon RDB projections as the primary data source for queries and current state (these primary Projectors extend a "PrimaryProjector" interface).

3. Support the regeneration of the projections to generate a state of the DB in the past (copy and prep DB and start the application). 

4. Eliminate the delay between Event persistence and RDB projections being current (sync vs async projection creation).

5. Demonstrate Barrette Lafrance's idea to support delete and restore using two RDB tables (active and deleted) and moving the row between them.

6. Support Data Seeding (loading) of Events (and resulting projections) on start up.

7. Support Bulk Loading of Events (and resulting projections) by appropriate system users.

8. Support easy DB population (Events and resulting projections) in UnitTests.

9. To ensure that new commands can properly enforce the business rules, the primary projections must be current (with the latest Event for its driving Aggregate Type) before new commands are processed.


## Branches

-  phase1ConvertBoot2Axon4      &nbsp;&nbsp;&nbsp; - 
                                Initial conversion to SpringBoot 2 & Axon 4
                                 
-  phase2SubscribingProjector   &nbsp;&nbsp; - 
                                Switch the primary projectors from the default TrackingEventProcessors into 
                                SubscribingEventProcessors in the hope that a failure in the projector will
                                terminate the process of recording the Event.  Unfortunately, multiple approaches
                                resulted in the same outcome: Event stored, and projector blocked on "bad" Event.
                                Because of these consistent outcomes, this approach was abandoned!
                                
-  phase3SyncWithTracker        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - 
                                Leveraging the synchronous concepts of another application (Work Orders Team) and
                                wanting to support past state generation.  Goal: 1, 2, & 3 
                                
-  phase4WithEndpointSecurity   &nbsp; - 
                                Support a reflection based Endpoint Path & User/Role collection tool to simplify
                                implementing SecurityConfig.  No in the Goals list, but WFH, this is a demo Repo!
                                
-  phase5WithDeleteRestore      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - 
                                Support the Commands & Events (and state management) to move rows in the projections
                                between the "Active" and "Deleted" tables.  Goal: 5
                                
-  phase6DataLoading            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; - 
                                Add two commands "...Load" and "...LoadOverwrite" and the ability to load a YAML file on start up.
                                Additionally, the "...LoadOverwrite" command can be "sent" to force the creation (or an update)
                                Event to be added (with the resulting projections created or updated) - assuming that the results
                                don't violate a business rule.  Support the check and waiting for the primary projections to be current.
                                Goals: 6, 7, 8, & 9 (have not implemented blocking new commands yet...)  

## Implementation

Implementation notes:
- The event store is backed by a JPA Event Store implementation which comes with Axon
- The query model is backed by a Spring Data JPA Repository
- The user interface is updated asynchronously via stompjs over websockets using Spring Websockets support

## Documentation

* Axon Framework - http://www.axonframework.org/
* Spring Boot - http://projects.spring.io/spring-boot/
* Spring Framework - http://projects.spring.io/spring-framework/
* Spring Data JPA - https://projects.spring.io/spring-data-jpa/
