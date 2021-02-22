package nl.avthart.todo.app.dto;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptionalsTest {

    @Test
    void checkJson()
            throws JsonProcessingException {
        Optionals zOptionals = Optionals.builder()
                .title( Optional.of("Fred") )
                .version( Optional.empty() )
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String zJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( zOptionals );
        System.out.println( "OptionalsTest.checkJson:" );
        System.out.println("Object:\n" + zOptionals);
        System.out.println("--------------------------");
        System.out.println("JSON:\n" + zJson);
    }
}