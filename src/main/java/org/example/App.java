package org.example;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.example.db.MongoDB;
import org.example.db.PostgreSQL;
import org.bson.Document;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class App
{
    public static void main( String[] args ) throws SQLException {
        // Connect to MongoDB
        MongoDB mongo = new MongoDB();
        mongo.connect();

        // Connect to PostgreSQL
        PostgreSQL psql = new PostgreSQL();
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";                                    // PostgreSQL username
        String password = "493075273";                               // PostgreSQL password
        psql.connect(url,user,password);

/// //////////////////////////////////////////////////
        MongoDatabase db =  mongo.mongoClient.getDatabase("mongoDB");
        String sql ;
        for(String c:db.listCollectionNames()){
            System.out.println("collections: "+ c );

//        Retrieve Doc in each collection
            MongoCollection<Document> collection = db.getCollection(c);

            for(Document doc:collection.find()){
// //              HashMap
                HashMap<String,Object> docData = new HashMap<>();

                for(String key:doc.keySet()) {
                    Object value = doc.get(key);

//       Save data from MongoDB into lists
                    docData.put(key,value);

                    System.out.println(key + " & the value: " + value);
                }

//       convert cols to string like:[_id,name,..] to ("_id,name,.."), to be valid for SQL statement
                String colStr = String.join(", ",docData.keySet());


                //       convert vals space to (?,?,..), to be valid for SQL statement
                String valStr = String.join(", ", Collections.nCopies(docData.keySet().size(), "?"));
                sql = "INSERT INTO "+c  +"(" + colStr + ") values ("+ valStr +")";
                PreparedStatement psmt = psql.conn.prepareStatement(sql);
                Context context=new Context();
                int i = 0;
                for (String key:docData.keySet()) {
                    Object originalValue = docData.get(key);

                    if (originalValue == null) {
                        psmt.setNull(i + 1, java.sql.Types.NULL );
                    } else if (originalValue instanceof Integer) {
                        psmt.setInt(i + 1, (Integer) originalValue);
                    } else if (originalValue instanceof Long) {
                        psmt.setLong(i + 1, (Long) originalValue);
                    } else if (originalValue instanceof Double) {
                        psmt.setDouble(i + 1, (Double) originalValue);
                    } else if (originalValue instanceof Boolean) {
                        psmt.setBoolean(i + 1, (Boolean) originalValue);
                    } else if (originalValue instanceof Date) {
                        psmt.setTimestamp(i + 1, new java.sql.Timestamp(((Date) originalValue).getTime()));
                    } else {
                        psmt.setString(i + 1, originalValue.toString());
                    }
                    i +=1;
                }

//              Error Handling
                try {
                    psmt.executeUpdate();
                    System.out.println("Inserted successfully!");
                }catch (Exception e){
                    System.out.println("Inserted Failed"+e.getMessage());
                }

            }
            System.out.println("--------------------------");
        }

















//
//        //  Close DBs
//        mongo.close();
//        psql.close();

    }
}