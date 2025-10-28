package org.example;


import com.mongodb.client.*;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;


public class MongoDB {
    public MongoClient mongoClient ;
    public HashMap<String,String> hashMap = new HashMap<>();

    public void connect() {
        this.mongoClient = MongoClients.create("mongodb://localhost:27017");
        System.out.println("Connected to MongoDB successfully!");
    }

    public void exportCollection(){
//        Retrieve the DB
        MongoDatabase db = this.mongoClient.getDatabase("mongoDB");

//        Retrieve Collections
        for(String c:db.listCollectionNames()){
            System.out.println("collections: "+ c );

//        Retrieve Doc in each collection
            MongoCollection<Document> collection = db.getCollection(c);
            System.out.println("__________________");

            for(Document doc:collection.find()){
                System.out.println(" docs: "  + doc);

//        Retrieve key & values in each doc
                for(String key:doc.keySet()){
                    String value = doc.get(key).toString();
                    System.out.println(key + " & the value: "+ value);
                    hashMap.put(key,value);
                }
                System.out.println("--------------------------");

            }

        }

        System.out.println(hashMap);

    }

    public void close(){
        this.mongoClient.close();
        System.out.println("Close MongoDB successfully!");
    }




    public static void main( String[] args ){
        MongoDB mongo = new MongoDB();
        mongo.connect();
        mongo.exportCollection();
    }
}

