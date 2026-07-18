package org.example.config;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MappingValidatorTest {

    // --- Helper: build a valid SINGLE mapping ---
    private ReadJSON.Mapping single(String mongoKey, String psqlCol) {
        return new ReadJSON.Mapping(mongoKey, psqlCol, "VARCHAR", ReadJSON.Shape.SINGLE);
    }

    // --- Happy path ---

    @Test
    void valid_flat_mappings_pass_without_exception() {
        List<ReadJSON.Mapping> mappings = List.of(
                single("_id",  "_id"),
                single("name", "name"),
                single("price","price")
        );
        // should not throw
        assertDoesNotThrow(() -> MappingValidator.validate(mappings));
    }

    @Test
    void valid_array_with_jsonb_type_passes() {
        List<ReadJSON.Mapping> mappings = List.of(
                single("_id", "_id"),
                new ReadJSON.Mapping("tags", "tags", "JSONB", ReadJSON.Shape.ARRAY)
        );
        assertDoesNotThrow(() -> MappingValidator.validate(mappings));
    }

    @Test
    void valid_object_with_jsonb_type_passes() {
        List<ReadJSON.Mapping> mappings = List.of(
                single("_id", "_id"),
                new ReadJSON.Mapping("meta", "meta", "JSONB", ReadJSON.Shape.OBJECT)
        );
        assertDoesNotThrow(() -> MappingValidator.validate(mappings));
    }

    // --- mongoKey errors ---

    @Test
    void blank_mongoKey_throws() {
        List<ReadJSON.Mapping> mappings = List.of(
                new ReadJSON.Mapping("", "name", "VARCHAR", ReadJSON.Shape.SINGLE)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MappingValidator.validate(mappings)
        );
        assertTrue(ex.getMessage().contains("mongoKey"));
    }

    @Test
    void duplicate_mongoKey_throws() {
        List<ReadJSON.Mapping> mappings = List.of(
                single("name", "name"),
                single("name", "name_copy")   // same mongoKey twice
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MappingValidator.validate(mappings)
        );
        assertTrue(ex.getMessage().contains("duplicate"));
    }

    // --- psqlCol errors ---

    @Test
    void blank_psqlCol_throws() {
        List<ReadJSON.Mapping> mappings = List.of(
                new ReadJSON.Mapping("name", "  ", "VARCHAR", ReadJSON.Shape.SINGLE)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MappingValidator.validate(mappings)
        );
        assertTrue(ex.getMessage().contains("psqlCol"));
    }

    @Test
    void duplicate_psqlCol_throws() {
        List<ReadJSON.Mapping> mappings = List.of(
                single("name",  "col"),
                single("price", "col")   // both map to same Postgres column
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MappingValidator.validate(mappings)
        );
        assertTrue(ex.getMessage().contains("duplicate"));
    }

    // --- type errors ---

    @Test
    void null_type_throws() {
        List<ReadJSON.Mapping> mappings = List.of(
                new ReadJSON.Mapping("name", "name", null, ReadJSON.Shape.SINGLE)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MappingValidator.validate(mappings)
        );
        assertTrue(ex.getMessage().contains("type"));
    }

    // --- shape + type conflict ---

    @Test
    void array_shape_with_non_jsonb_type_throws() {
        List<ReadJSON.Mapping> mappings = List.of(
                new ReadJSON.Mapping("tags", "tags", "INTEGER", ReadJSON.Shape.ARRAY)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MappingValidator.validate(mappings)
        );
        assertTrue(ex.getMessage().contains("JSONB"));
    }

    @Test
    void object_shape_with_non_jsonb_type_throws() {
        List<ReadJSON.Mapping> mappings = List.of(
                new ReadJSON.Mapping("meta", "meta", "VARCHAR", ReadJSON.Shape.OBJECT)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MappingValidator.validate(mappings)
        );
        assertTrue(ex.getMessage().contains("JSONB"));
    }
}