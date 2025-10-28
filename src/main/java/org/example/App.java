package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class App
{
    public static void main( String[] args )
    {
        // Connect to MongoDB
        MongoDB mongo = new MongoDB();
        mongo.connect();

        // Connect to PostgreSQL
        PostgreSQL psql = new PostgreSQL();
        psql.connect();

/// //////////////////////////////////////////////////
        MongoDatabase db =  mongo.mongoClient.getDatabase("mongoDB");

//        HashMap<String,String> hashMap = new HashMap<>();
        String sql ;



        for(String c:db.listCollectionNames()){
            System.out.println("collections: "+ c );

//        Retrieve Doc in each collection
            MongoCollection<Document> collection = db.getCollection(c);

            for(Document doc:collection.find()){
                List<String> columns = new ArrayList<String>();
                List<String> values =  new ArrayList<String>();

//                if(c.equals("orders")){

                    for(String key:doc.keySet()) {
                        Object value = doc.get(key);

//       Save data from MongoDB into lists
                        columns.add(key);
                        values.add(value != null ? value.toString():null);

                        System.out.println(key + " & the value: " + value);
                    }

//       convert cols to string like:[_id,name,..] to ("_id,name,.."), to be valid for SQL statement
                    String colStr = String.join(", ",columns);
//       convert vals space to (?,?,..), to be valid for SQL statement
                    String valStr = String.join(", ", Collections.nCopies(columns.size(), "?"));
                    sql = "INSERT INTO "+c  +"(" + colStr + ") values ("+ valStr +")";


                    try (PreparedStatement psmt = psql.conn.prepareStatement(sql)){
//       insert values into SQL statement
                        for (int i = 0; i < values.size(); i++) {
                            String value = values.get(i);
                            Object originalValue = doc.get(columns.get(i));

                            if (value == null) {
                                psmt.setNull(i + 1, java.sql.Types.NULL);
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
                                psmt.setString(i + 1, value);
                            }
                        }
                        psmt.executeUpdate();
                        System.out.println("Inserted successfully!");

                    }catch (Exception e){
                        System.out.println("Inserted FFFFFFFFFFFf"+e.getMessage());
                    }
//                }


////        Retrieve key & values in each doc , and store in HashMap
//                for(String key:doc.keySet()){
//                    String value = doc.get(key).toString();
//                    System.out.println(key + " & the value: "+ value);
//                    hashMap.put(key,value);
//                }


            }
            System.out.println("--------------------------");
        }

//        System.out.println(hashMap);

















//
//        //  Close DBs
//        mongo.close();
//        psql.close();

    }
}
