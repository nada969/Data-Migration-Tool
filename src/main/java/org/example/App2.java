package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.config.ReadJSON;
import org.example.db.MongoDB;
import org.example.db.PostgreSQL;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.jooq.SQLDialect.*;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.*;

public class App2 {
    public static void main( String[] args ) throws IOException, SQLException {
//        1- Configure JSON File
//        define Jackson mapper
        ObjectMapper mapper = new ObjectMapper();
//        insert JSON file path
        File file = new File(".//src//main//java//org//example//config//conf.json");
//        mapping JSON file into --> ReadJSON class
        ReadJSON readJSON = mapper.readValue(file,ReadJSON.class);

//      2- connect to Data Base
//      Connect to MongoDB
        MongoDB mongo = new MongoDB();
        mongo.connect();
//      mongo db name(source)
        String source = readJSON.getSource();
        MongoDatabase db = mongo.mongoClient.getDatabase(source);
//      mongo collection name (collectionName)
        String collectionName = readJSON.getCollectionName();
        MongoCollection<Document> collection = db.getCollection(collectionName);

//      Connect to PostgreSQL
        PostgreSQL psql = new PostgreSQL();
        String url = readJSON.getUrlDestination() ;
        String user = readJSON.getDestination();
        String password= readJSON.getDestinationPassWord();
        psql.connect(url,user,password);

        String sql1;
        String sql2;
        String sql3;

        String table_name = readJSON.getTableName();

//       3- Tables
//      check if the table exist in PostgreSQL ("tableName"):
        sql1 = "CREATE TABLE IF NOT EXISTS " + table_name +
                " (id SERIAL PRIMARY KEY)";
        Statement stmt1 = psql.conn.createStatement();
        stmt1.executeUpdate(sql1);

//      4- Columns
//      check if each column exists in that table
//      loop over Mapping to create the columns if not exist
        for(ReadJSON.Mapping map: readJSON.getMappings()){
            sql2 = "Alter Table " + table_name + " " +
                    " Add column If not exists " + map.psqlCol() + " " + map.type() ;

            try (Statement stmt = psql.conn.createStatement()) {
                stmt.executeUpdate(sql2);
            }
        }

//      5- Values
//      first: loop all over the docs in the collection
//      sec: loop to insert all values of this doc, in the columns that defined above
//        Using ( JOOQ )
//        Collection
//             ├── Document #1
//             │     ├── mapping loop
//             │     └── INSERT row --> insert each value alone
//             ├── Document #2
//             │     ├── mapping loop
//             │     └── INSERT row  --> insert each value alone
        DSLContext create = DSL.using(
                readJSON.getUrlDestination(),
                "postgres",
                readJSON.getDestinationPassWord()
        );
        for (Document document : collection.find()) {
            for (ReadJSON.Mapping map : readJSON.getMappings()) {
                //          Array value: --> table with one col & n.th rows
                if (map.type().equals("array")) {
                    continue;
                }
                //          Object value: --> table with one row & n.th cols
                else if (map.type().equals("Object")) {
                    continue;
                }
                //          Single value: --> value
                else {
                    Object value = document.get(map.mongoKey());
                    create.insertInto(table_name.toString())
                            .Set(map.psqlCol(),value)
                            .execute();
                }



                
            }
        }
        }

//        int paramIndex = 1;
//        for (Object mapping : mappings) {
//            Object value = document.get(mapping.getMongoKey());
//            TypeHandler.setValue(pstmt, paramIndex, value, mapping.getType());
//            paramIndex++;
//        }

//        loop for each doc in the collection:  Extract data from MongoCollection & put them in HashTable
//        for(Document doc:collection.find()){
//
//            HashMap<String,Object> docData = new HashMap<>();
//            for(String key:doc.keySet()) {
//                Object value = doc.get(key);
//
////          Save data from MongoDB into lists
//                docData.put(key,value);
//                System.out.println(key + " & the value: " + value);
//            }
//
//            String colStr = String.join(", ",docData.keySet());
//            String valStr = String.join(", ", Collections.nCopies(docData.keySet().size(), "?"));
//
////          convert vals space to (?,?,..), to be valid for SQL statement
//            sql2 = "INSERT INTO "+readJSON.getTableName()  +"(" + colStr + ") values ("+ valStr +")";
//            PreparedStatement stmt2 = psql.conn.prepareStatement(sql2);
//
//            int i = 0;
//            for (String key:docData.keySet()) {
//                Object originalValue = docData.get(key);
//                System.out.println("value: "+originalValue.toString()+". the type:"+originalValue.getClass().getSimpleName());
//                TypeHandler.setValue(stmt2,i+1,originalValue.getClass().getSimpleName(),key);
//
//                try {
//                    stmt2.executeUpdate();
//                    System.out.println("Inserted Done");
//                }
//                catch (Exception e){
//                    System.out.println("Inserted Failed"+e.getMessage());
//                }
//
//                i +=1;
//            }
//            System.out.println("--------------------------------------");
//        }
//    }
}
