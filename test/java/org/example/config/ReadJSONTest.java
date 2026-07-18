package java.org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.ReadJSON;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class ReadJSONTest {

    @Test
    void conf_json_deserializes_correctly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ReadJSON config = mapper.readValue(
                new File("src/main/resources/conf.json"),
                ReadJSON.class
        );

        // top-level fields
        assertEquals("mongoDB",   config.getSource());
        assertEquals("products",  config.getCollectionName());
        assertEquals("products",  config.getTableName());
        assertNotNull(config.getMappings());
        assertFalse(config.getMappings().isEmpty());
    }

    @Test
    void shape_field_deserializes_to_correct_enum() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ReadJSON config = mapper.readValue(
                new File("src/main/resources/conf.json"),
                ReadJSON.class
        );

        // find the ARRAY mapping and confirm shape parsed correctly
        ReadJSON.Mapping arrayMapping = config.getMappings().stream()
                .filter(m -> m.mongoKey().equals("protectedParameters"))
                .findFirst()
                .orElseThrow();

        assertEquals(ReadJSON.Shape.ARRAY, arrayMapping.shape());
        assertEquals("JSONB", arrayMapping.type());
    }

    @Test
    void missing_shape_in_json_defaults_to_single() throws Exception {
        // test the compact record with no shape field
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {"mongoKey": "name", "psqlCol": "name", "type": "VARCHAR"}
            """;
        ReadJSON.Mapping mapping = mapper.readValue(json, ReadJSON.Mapping.class);
        assertEquals(ReadJSON.Shape.SINGLE, mapping.shape());
    }
}