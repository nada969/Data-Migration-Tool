package org.example.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MappingValidator {

    // Holds one problem found in the config
    public record ValidationError(int index, String field, String reason) {
        @Override
        public String toString() {
            return "  mapping[" + index + "] → " + field + ": " + reason;
        }
    }

    public static void validate(List<ReadJSON.Mapping> mappings) {
        List<ValidationError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Set<String> seenMongo = new HashSet<>();
        Set<String> seenPsql  = new HashSet<>();

        for (int i = 0; i < mappings.size(); i++) {
            ReadJSON.Mapping map = mappings.get(i);

            // 1. mongoKey must not be null or blank
            if (map.mongoKey() == null || map.mongoKey().isBlank()) {
                errors.add(new ValidationError(i, "mongoKey", "must not be null or blank"));
            } else if (!seenMongo.add(map.mongoKey())) {
                errors.add(new ValidationError(i, "mongoKey",
                        "duplicate — '" + map.mongoKey() + "' appears more than once"));
            }

            // 2. psqlCol must not be null or blank
            if (map.psqlCol() == null || map.psqlCol().isBlank()) {
                errors.add(new ValidationError(i, "psqlCol", "must not be null or blank"));
            } else if (!seenPsql.add(map.psqlCol().trim())) {
                errors.add(new ValidationError(i, "psqlCol",
                        "duplicate — '" + map.psqlCol() + "' appears more than once"));
            }

            // 3. type must not be null or blank
            if (map.type() == null || map.type().isBlank()) {
                errors.add(new ValidationError(i, "type", "must not be null or blank"));
            }

            // 4. shape defaults to SINGLE if missing — log a warning, not an error
            if (map.shape() == null) {
                warnings.add("  mapping[" + i + "] has no shape — defaulting to SINGLE");
            }

            // 5. ARRAY or OBJECT shape should use JSONB as SQL type
            if (map.shape() == ReadJSON.Shape.ARRAY || map.shape() == ReadJSON.Shape.OBJECT) {
                if (map.type() != null && !map.type().equalsIgnoreCase("JSONB")) {
                    errors.add(new ValidationError(i, "type",
                            "shape is " + map.shape() + " but type is '" + map.type()
                                    + "' — expected JSONB"));
                }
            }
        }

        // Print warnings (non-fatal)
        if (!warnings.isEmpty()) {
            System.out.println("[MappingValidator] WARNINGS:");
            warnings.forEach(System.out::println);
        }

        // Throw on errors (fatal — stop before touching any database)
        if (!errors.isEmpty()) {
            StringBuilder msg = new StringBuilder(
                    "\n[MappingValidator] Invalid mappings.json — fix these before running:\n");
            errors.forEach(e -> msg.append(e.toString()).append("\n"));
            throw new IllegalArgumentException(msg.toString());
        }

        System.out.println("[MappingValidator] Config OK — " + mappings.size() + " mappings validated.");
    }
}