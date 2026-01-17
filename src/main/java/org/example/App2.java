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
        File file = new File(".//src//main//java//org//example//config//conf.json");
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
        String sql3;


//        mongo db name(source)
        String source = readJSON.getSource();
        MongoDatabase db = mongo.mongoClient.getDatabase(source);

//        mongo collection name (collectionName)
        String collectionName = readJSON.getCollectionName();
        MongoCollection<Document> collection = db.getCollection(collectionName);

//      check if the table exist in PostgreSQL ( "tableName"):
        sql1 = "SELECT EXISTS (" +
                "  SELECT 1 FROM information_schema.tables " +
                "  WHERE table_schema = 'public' " +
                "  AND table_name = ' " +readJSON.getTableName()+ "'" +
                ")";

//        loop to create all the columns
//        Object mappings = readJSON.getMappings();
//        int paramIndex = 1;
//        for (Object mapping : mappings) {
//            Object value = document.get(mapping.getMongoKey());
//            TypeHandler.setValue(pstmt, paramIndex, value, mapping.getType());
//            paramIndex++;
//        }

        sql2 = "CREATE TABLE " + readJSON.getTableName() + " (id SERIAL PRIMARY KEY)";


        try(PreparedStatement psmt1 = psql.conn.prepareStatement(sql1)){
            psmt1.executeQuery();
        }
        catch (Exception e){
            PreparedStatement psmt2 = psql.conn.prepareStatement(sql2);
            psmt2.executeQuery();
        }


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
            sql3 = "INSERT INTO "+readJSON.getTableName()  +"(" + colStr + ") values ("+ valStr +")";
            PreparedStatement psmt3 = psql.conn.prepareStatement(sql3);
            Context context=new Context();
            int i = 0;
            for (String key:docData.keySet()) {
                Object originalValue = docData.get(key);

                if (originalValue == null) {
                    psmt3.setNull(i + 1, java.sql.Types.NULL );
                } else if (originalValue instanceof Integer) {
                    psmt3.setInt(i + 1, (Integer) originalValue);
                } else if (originalValue instanceof Long) {
                    psmt3.setLong(i + 1, (Long) originalValue);
                } else if (originalValue instanceof Double) {
                    psmt3.setDouble(i + 1, (Double) originalValue);
                } else if (originalValue instanceof Boolean) {
                    psmt3.setBoolean(i + 1, (Boolean) originalValue);
                } else if (originalValue instanceof Date) {
                    psmt3.setTimestamp(i + 1, new java.sql.Timestamp(((Date) originalValue).getTime()));
                } else {
                    psmt3.setString(i + 1, originalValue.toString());
                }
                i +=1;
            }

//              Error Handling
            try {
                psmt3.executeUpdate();
                System.out.println("Inserted Done");
            }
            catch (Exception e){
                System.out.println("Inserted Failed"+e.getMessage());
            }
        }
    }
}
