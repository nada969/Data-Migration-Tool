package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.config.ReadJSON;
import org.example.db.MongoDB;
import org.example.db.PostgreSQL;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
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

//          Save data from MongoDB into lists
                docData.put(key,value);
                System.out.println(key + " & the value: " + value);
            }

            String colStr = String.join(", ",docData.keySet());
            String valStr = String.join(", ", Collections.nCopies(docData.keySet().size(), "?"));

//          convert vals space to (?,?,..), to be valid for SQL statement
            sql = "INSERT INTO "+readJSON.getTableName()  +"(" + colStr + ") values ("+ valStr +")";
//            sql1 = "SELECT count(*) FROM information_schema.tables " +
//                    "WHERE table_name = '" + readJSON.getTableName() + "' LIMIT 1;";
//            sql2 = "CREATE TABLE " + readJSON.getTableName() + " (id SERIAL PRIMARY KEY)";
            try {
                PreparedStatement psmt = psql.conn.prepareStatement(sql+sql);
                psmt.executeUpdate();
            }
            catch (Exception e){
                System.out.println("Inserted Failed"+e.getMessage());
            }
        }
    }
}
