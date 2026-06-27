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

            if (value instanceof ObjectId objectId) {
                value = objectId.toHexString();
            }

//            step = (step == null)
//                    ? insert.set(DSL.field(map.psqlCol()), value)
//                    : step.set(DSL.field(map.psqlCol()), value);
            parentRow.put(map.psqlCol(), value);

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
