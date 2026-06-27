package org.example.writer;

import org.example.config.ReadJSON;
import org.example.schema.SchemaWalker.WalkResult;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/// PSQL
///
///         String sql1;
///         String sql2 ="";
///
///         String table_name = readJSON.getTableName();
///
/// //       3- Tables ((Table creation Data Definition Language (DDL)))
/// //      check if the table exist in PostgreSQL ("tableName"):
///         sql1 = "CREATE TABLE IF NOT EXISTS " + table_name +
///                 " (id SERIAL PRIMARY KEY)";
///         Statement stmt1 = psql.conn.createStatement();
///         stmt1.executeUpdate(sql1);
///
///
/// //      4- Columns
/// //      check if each column exists in that table
/// //      loop over Mapping to create the columns if not exist
///         for (ReadJSON.Mapping map : readJSON.getMappings()) {
///             sql2 = "Alter Table " + table_name + " " +
///                     " Add column If not exists " + map.psqlCol() + " " + map.type();
///
///             try (Statement stmt = psql.conn.createStatement()) {
///                 stmt.executeUpdate(sql2);
///             }
///         }
///
/// var insert = create.insertInto(DSL.table(table_name));
///
///             InsertSetMoreStep<Record> step = null;
///             loop map
///             if (step != null) step.execute();
public class RowWriter {

    private final DSLContext create;
    private final Connection conn;
    private final String tableName;
    private final List<ReadJSON.Mapping> mappings;

    public RowWriter(DSLContext create, Connection conn,
                     String tableName, List<ReadJSON.Mapping> mappings) {
        this.create    = create;
        this.conn      = conn;
        this.tableName = tableName;
        this.mappings  = mappings;
    }

    // --- DDL (moved from App2 steps 3 and 4) ---

    // Step 3: create the table if it does not exist yet
    public void ensureTableExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName
                + " (id SERIAL PRIMARY KEY)";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    // Step 4: add a column for each mapping if it does not exist yet
    public void ensureColumnsExist() throws SQLException {
        for (ReadJSON.Mapping map : mappings) {
            String sql = "ALTER TABLE " + tableName
                    + " ADD COLUMN IF NOT EXISTS "
                    + map.psqlCol() + " " + map.type();
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    // --- INSERT (moved from App2 step 5) ---

    // Takes the result of SchemaWalker.walk() and executes one INSERT.
    // Batching (Week 5) and retry logic (Week 6) get added here later
    // without touching SchemaWalker or App at all.
    public void insert(WalkResult result) {
        var insert = create.insertInto(DSL.table(tableName));
        InsertSetMoreStep<Record> step = null;

        for (Map.Entry<String, Object> entry : result.parentRow().entrySet()) {
            step = (step == null)
                    ? insert.set(DSL.field(entry.getKey()), entry.getValue())
                    : step.set(DSL.field(entry.getKey()), entry.getValue());
        }

        if (step != null) step.execute();
    }

}
