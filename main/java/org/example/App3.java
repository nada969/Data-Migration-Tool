package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.config.ReadJSON;
import org.example.connector.MongoDB;
import org.example.connector.PostgreSQL;
import org.example.schema.SchemaWalker;
import org.example.schema.SchemaWalker.WalkResult;
import org.example.writer.RowWriter;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.File;

public class App3 {

    public static void main(String[] args) throws Exception {

        // 1. Read config
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("main/java/org/example/config/conf.json");
        ReadJSON readJSON = mapper.readValue(file, ReadJSON.class);

        // 2. Connect
        MongoDB mongo = new MongoDB();
        mongo.connect();
        MongoDatabase db = mongo.mongoClient.getDatabase(readJSON.getSource());
        MongoCollection<Document> collection = db.getCollection(readJSON.getCollectionName());

        PostgreSQL psql = new PostgreSQL();
        psql.connect(
                readJSON.getUrlDestination(),
                readJSON.getDestination(),
                readJSON.getDestinationPassWord()
        );

        // 3. Set up table + columns (DDL lives inside RowWriter)
        RowWriter rowWriter = new RowWriter(
                DSL.using(psql.conn, SQLDialect.POSTGRES),
                psql.conn,
                readJSON.getTableName(),
                readJSON.getMappings()
        );
        rowWriter.ensureTableExists();
        rowWriter.ensureColumnsExist();

        // 4. Walk + write each document
        SchemaWalker walker = new SchemaWalker(readJSON.getMappings());

        System.out.println("Starting migration...");
        for (Document document : collection.find()) {
            WalkResult result = walker.walk(document);
            rowWriter.insert(result);
        }
        System.out.println("Migration complete.");

        // 5. Close connections
        mongo.close();
        psql.close();
    }
}