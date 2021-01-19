package nl.avthart.todo.app.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndpointTest {

    @Test
    void checkUpdatedData() {
        assertFalse( Endpoint.checkUpdatedData( "Nope" ) );
        assertFalse( Endpoint.checkUpdatedData( "2011-01-16" ) );
        assertFalse( Endpoint.checkUpdatedData( "2019-06-30" ) );
        assertFalse( Endpoint.checkUpdatedData( "2020-02-29-" ) );
        assertFalse( Endpoint.checkUpdatedData( ", 2020-02-29" ) );
        assertFalse( Endpoint.checkUpdatedData( ",2020-02-29" ) );
        assertFalse( Endpoint.checkUpdatedData( "-2020-02-29" ) );
        assertTrue( Endpoint.checkUpdatedData( "2020-02-29" ) );
        assertTrue( Endpoint.checkUpdatedData( "2020-02-29," ) );
        assertTrue( Endpoint.checkUpdatedData( "2020-02-29 , " ) );
        assertTrue( Endpoint.checkUpdatedData( "2020-02-29 , 2021-01-16" ) );
        assertFalse( Endpoint.checkUpdatedData( "02-16-2020" ) );
        assertFalse( Endpoint.checkUpdatedData( "16-02-2020" ) );
    }

    @Test
    void isLeapYear() {
        assertFalse( Endpoint.isLeapYear( 1899 ) );
        assertFalse( Endpoint.isLeapYear( 1900 ) );
        assertTrue( Endpoint.isLeapYear( 2000 ) );
        assertTrue( Endpoint.isLeapYear( 2020 ) );
        assertFalse( Endpoint.isLeapYear( 2021 ) );
        assertTrue( Endpoint.isLeapYear( 2024 ) );
        assertFalse( Endpoint.isLeapYear( 2100 ) );
    }
}