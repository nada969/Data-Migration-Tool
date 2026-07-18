package org.example.schema;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.config.ReadJSON;

import java.util.LinkedHashMap;
import java.util.Map;


/// mongo
/// Specifically SchemaWalker owns:
/// Extracting a value from a document by mongoKey
/// Converting the BSON type to something SQL can accept (ObjectId → String is already there, Date, Decimal128 etc. come later)
/// Deciding the shape — is this value a scalar, an object, or an array? (this is what your commented-out code was trying to do)
/// Returning a clean result that RowWriter can blindly insert — SchemaWalker should know nothing about SQL or jOOQ
///

public class SchemaWalker {
    private final java.util.List<ReadJSON.Mapping> mappings;

    public SchemaWalker(java.util.List<ReadJSON.Mapping> mappings){
        this.mappings = mappings;
    }

    public record WalkResult(Map<String,Object> parentRow){}
    public WalkResult walk(Document document) {
        Map<String, Object> parentRow = new LinkedHashMap<>();
        for (ReadJSON.Mapping map : mappings) {
            Object value = document.get(map.mongoKey());
            if (value == null) {
                parentRow.put(map.psqlCol(), null);  // explicit null → SQL NULL
                continue;                             // skip type conversion + shape switch
            }
            if (value instanceof ObjectId objectId) {
                value = objectId.toHexString();
            }
            switch (map.shape()) {
                // extract scalar value → parent column
                case SINGLE ->
                        parentRow.put(map.psqlCol(), value);

//                case OBJECT -> // flatten nested document → prefixed parent columns
//                        parentRow.put(map.psqlCol(), value);
//
//                case ARRAY  -> // produce child-table rows
//                        parentRow.put(map.psqlCol(), value);

            }

            System.out.printf("key: ");
            System.out.println(map.mongoKey());
            System.out.printf("value: ");
            System.out.println(value);
            System.out.printf("class: ");
            System.out.println(value != null? value.getClass():"");
            System.out.println("---------------");

        }
        return new WalkResult(parentRow);
    }


}
