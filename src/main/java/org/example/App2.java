package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class App2 {
    public static void main( String[] args ) throws IOException {

        // Connect to MongoDB
        MongoDB mongo = new MongoDB();
        mongo.connect();

        // Connect to PostgreSQL
        PostgreSQL psql = new PostgreSQL();
        psql.connect();
        String sql;

//        define Jackson mapper
        ObjectMapper mapper = new ObjectMapper();
//        insert JSON file path
        File file = new File(".//src//main//conf.json");

//        mapping JSON file into --> ReadJSON class
        ReadJSON readJSON = mapper.readValue(file,ReadJSON.class);

//        mongo db name(source)
        String source = readJSON.source;
        MongoDatabase db = mongo.mongoClient.getDatabase(source);
//        mongo collection name (collectionName)
        String collectionName = readJSON.collectionName;
        MongoCollection<Document> collection = db.getCollection(collectionName);


//        for(Document doc:collection.find()) {
//            HashMap<String, Object> docData = new HashMap<>();
//
//            for (String key : doc.keySet()) {
//                Object value = doc.get(key);
//                docData.put(key,value);
//
//                System.out.println(key + " : " + value);
//
//            }
//
//
//            String colStr = String.join(", ",docData.keySet());
//
//
//            //       convert vals space to (?,?,..), to be valid for SQL statement
//            String valStr = String.join(", ", Collections.nCopies(docData.keySet().size(), "?"));
//
////          Postgres tableName (tableName)
//
//            sql = "INSERT INTO "+" tableName " +"(" + colStr + ") values ("+ valStr +")";
//
//            try (PreparedStatement psmt = psql.conn.prepareStatement(sql)){
//                int i = 0;
//                for (String key:docData.keySet()) {
//                    Object originalValue = docData.get(key);
//
//                    if (originalValue == null) {
//                        psmt.setNull(i + 1, java.sql.Types.NULL);
//                    } else if (originalValue instanceof Integer) {
//                        psmt.setInt(i + 1, (Integer) originalValue);
//                    } else if (originalValue instanceof Long) {
//                        psmt.setLong(i + 1, (Long) originalValue);
//                    } else if (originalValue instanceof Double) {
//                        psmt.setDouble(i + 1, (Double) originalValue);
//                    } else if (originalValue instanceof Boolean) {
//                        psmt.setBoolean(i + 1, (Boolean) originalValue);
//                    } else if (originalValue instanceof Date) {
//                        psmt.setTimestamp(i + 1, new java.sql.Timestamp(((Date) originalValue).getTime()));
//                    } else {
//                        psmt.setString(i + 1, originalValue.toString());
//                    }
//                    i +=1;
//                }
//                psmt.executeUpdate();
//                System.out.println("Inserted successfully!");
//
//            }catch (Exception e){
//                System.out.println("Inserted FFFFFFFFFFFf"+e.getMessage());
//            }
//
//
//        }

    }
}
