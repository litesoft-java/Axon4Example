package nl.avthart.todo.app.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class CommonFieldsTest {

    @Test
    void checkJson()
            throws JsonProcessingException {
        CommonFields cf = new CommonFields()
                .withTitle( "Fred" )
                .withVersion( null );
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( cf );
        System.out.println( "CommonFieldsTest.checkJson:" );
        System.out.println( "Object:\n" + cf );
        System.out.println( "--------------------------" );
        System.out.println( "JSON:\n" + json );

        CommonFields cf2 = mapper.readValue( json, CommonFields.class );
        System.out.println( "cf2:\n" + cf2 );

        Assert.assertTrue( cf.isEquivalent( cf2 ) );
    }
}