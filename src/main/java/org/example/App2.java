package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class App2 {
    public static void main( String[] args ) throws IOException, SQLException {
//        define Jackson mapper
        ObjectMapper mapper = new ObjectMapper();
//        insert JSON file path
        File file = new File(".//src//main//conf.json");

//        mapping JSON file into --> ReadJSON class
        ReadJSON readJSON = mapper.readValue(file,ReadJSON.class);

        // Connect to MongoDB
        MongoDB mongo = new MongoDB();
        mongo.connect();

        // Connect to PostgreSQL
        PostgreSQL psql = new PostgreSQL();
        String url = readJSON.getUrlDestination() ;
        String user = readJSON.getDestination();
        String password= readJSON.getDestinationPassWord();
        psql.connect(url,user,password);
        String sql1;
        String sql2;
        String sql;


//        mongo db name(source)
        String source = readJSON.getSource();
        MongoDatabase db = mongo.mongoClient.getDatabase(source);

//        mongo collection name (collectionName)
        String collectionName = readJSON.getCollectionName();
        MongoCollection<Document> collection = db.getCollection(collectionName);


//        loop for each doc in the collection:  Extract data from MongoCollection & put them in HashTable
        for(Document doc:collection.find()){

            HashMap<String,Object> docData = new HashMap<>();
            for(String key:doc.keySet()) {
                Object value = doc.get(key);

//       Save data from MongoDB into lists
                docData.put(key,value);
                System.out.println(key + " & the value: " + value);
            }

            String colStr = String.join(", ",docData.keySet());
            String valStr = String.join(", ", Collections.nCopies(docData.keySet().size(), "?"));



            sql = "INSERT INTO "+readJSON.getTableName()  +"(" + colStr + ") values ("+ valStr +")";
            sql1 = "SELECT count(*) FROM information_schema.tables " +
                    "WHERE table_name = '" + readJSON.getTableName() + "' LIMIT 1;";
            sql2 = "CREATE TABLE " + readJSON.getTableName() + " (id SERIAL PRIMARY KEY)";
            try {
                PreparedStatement psmt1 = psql.conn.prepareStatement(sql1);
                psmt1.executeQuery();

                PreparedStatement psmt = psql.conn.prepareStatement(sql);
                int i = 0;
//                for (String key : docData.keySet()) {
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
//                    i += 1;
//                }
                psmt.executeUpdate();
            }
            catch (Exception e){
//need to fix
//                PreparedStatement psmt2 = psql.conn.prepareStatement(sql2);
//                psmt2.executeQuery();
//
//                PreparedStatement psmt = psql.conn.prepareStatement(sql);
//                int i = 0;
//                for (String key : docData.keySet()) {
//                    Object originalValue = docData.get(key);
//
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
//                    i += 1;
//                }
//                psmt.executeUpdate();

                System.out.println("Inserted FFFFFFFFFFFf"+e.getMessage());
            }
        }
    }
}
