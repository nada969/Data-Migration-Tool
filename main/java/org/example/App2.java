package org.example;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.config.ReadJSON;
import org.example.connector.MongoDB;
import org.example.connector.PostgreSQL;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.*;


/// the goal: read config → connect → for each document: walk it, write the result → close connections
///
public class App2 {
    public static void main(String[] args) throws IOException, SQLException {
//        1- Configure JSON File
//        define Jackson mapper
        ObjectMapper mapper = new ObjectMapper();
//        insert JSON file path
        File file = new File("main/java/org/example/config/conf.json");
//        mapping JSON file into --> ReadJSON class
        ReadJSON readJSON = mapper.readValue(file, ReadJSON.class);

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
        String url = readJSON.getUrlDestination();
        String user = readJSON.getDestination();
        String password = readJSON.getDestinationPassWord();
        //                 PostgreSQL
        //                      ▲
        //                      │
        //             JDBC Connection
        //                      ▲
        //                psql.conn
        //                      ▲
        //                      │
        //        DSLContext (jOOQ)
        //                      ▲
        //                      │
        //       insert / update / select
        psql.connect(url, user, password);
        DSLContext create = DSL.using(psql.conn, SQLDialect.POSTGRES);


        String sql1;
        String sql2 ="";

        String table_name = readJSON.getTableName();

//       3- Tables ((Table creation Data Definition Language (DDL)))
//      check if the table exist in PostgreSQL ("tableName"):
        sql1 = "CREATE TABLE IF NOT EXISTS " + table_name +
                " (id SERIAL PRIMARY KEY)";
        Statement stmt1 = psql.conn.createStatement();
        stmt1.executeUpdate(sql1);


//      4- Columns
//      check if each column exists in that table
//      loop over Mapping to create the columns if not exist
        for (ReadJSON.Mapping map : readJSON.getMappings()) {
            sql2 = "Alter Table " + table_name + " " +
                    " Add column If not exists " + map.psqlCol() + " " + map.type();

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
        System.out.println("start Inserting data...");

        for (Document document : collection.find()) {
//            for (ReadJSON.Mapping map : readJSON.getMappings()) {
//                //          Array value: --> table with one col & n.th rows
//                if (map.type().equals("array")) {
//                    continue;
//                }
//                //          Object value: --> table with one row & n.th cols
//                else if (map.type().equals("Object")) {
//                    continue;
//                }
//                //          Single value: --> value
//                else {
//                    Object value = document.get(map.mongoKey());
//                    create.insertInto(DSL.table(table_name))
//                            .set(map.psqlCol().toString(),value)
//                            .execute();
//                }

            var insert = create.insertInto(DSL.table(table_name));

            InsertSetMoreStep<Record> step = null;

            for (ReadJSON.Mapping map : readJSON.getMappings()) {
                Object value = document.get(map.mongoKey());

                if (value instanceof ObjectId objectId) {
                    value = objectId.toHexString();
                }

                step = (step == null)
                        ? insert.set(DSL.field(map.psqlCol()), value)
                        : step.set(DSL.field(map.psqlCol()), value);
                System.out.printf("key: ");
                System.out.println(map.mongoKey());
                System.out.printf("value: ");
                System.out.println(value);
                System.out.printf("class: ");
                System.out.println(value.getClass());
                System.out.println("---------------");
            }
            System.out.println("step 4");

            if (step != null) step.execute();
            System.out.println("step 5");


        }

        System.out.println("Inserting data...");

        /// / Close Connections:
        mongo.close();
        psql.close();

    }
}
