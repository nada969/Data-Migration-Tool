# Migration tool — architecture overview

## How a document flows through the system

1. `App` reads `mappings.json`, opens a Mongo connection and a Postgres connection.
2. `App` asks `SchemaWalker` to split each document into one parent row plus zero or more child rows, based on the SINGLE / OBJECT / ARRAY shape declared per field in the config.
3. `RowWriter` inserts the parent row into the main table and gets back its generated id.
4. `ChildTableWriter` inserts the child rows into per-array child tables, each one tagged with the parent's id as a foreign key.
5. `RunLogger` records the outcome of the whole run — start time, end time, fetched/inserted/failed counts — in a `migration_run_log` table.
6. `App` closes both connections, whatever happened.

Anything nested deeper than one array level (an object inside an array item, or an array inside an array item) is not decomposed further — it gets stored as a JSONB string inside the child row. That's the deliberate depth cutoff agreed for this MVP.

---

## Folder structure

```
src/main/java/org/example/
├── App.java                       orchestration only — no business logic
├── connectors/
│   ├── MongoConnector.java
│   └── PostgresConnector.java
├── schema/
│   ├── Mapping.java                mongoKey, psqlCol, type, shape
│   ├── MappingValidator.java
│   └── SchemaWalker.java           splits a document into parent + child rows
├── writer/
│   ├── RowWriter.java              batched parent-row inserts
│   └── ChildTableWriter.java       child-table DDL + inserts
├── config/
│   ├── ReadJSON.java                maps mappings.json -> config object
│   └── ConfigGenerator.java         samples documents, writes starter mappings.json
└── log/
    └── RunLogger.java               writes to migration_run_log table
```

---

## What each piece is responsible for

**App**
Top-to-bottom orchestration only: read config, connect, walk, write, log, close. No SQL strings and no Mongo cursor logic should live here — if you find yourself writing a query inside `App`, it belongs in one of the other packages instead.

**connectors/MongoConnector**
Owns the MongoDB client lifecycle. Knows how to connect and how to hand back a collection. Nothing else — no field logic, no document inspection.

**connectors/PostgresConnector**
Owns a pooled connection to PostgreSQL (HikariCP). Hands out connections to whoever needs to run SQL. Nothing else.

**schema/Mapping**
A plain data holder: which MongoDB field, which PostgreSQL column, what type, and which of the three shapes (SINGLE, OBJECT, ARRAY) it is. No logic — just structure.

**schema/MappingValidator**
Checks the parsed config on startup and fails fast with a clear message if something is malformed or contradictory, rather than letting a bad config surface as a confusing runtime error later.

**schema/SchemaWalker**
The core of the new scope. Given one document and the mapping list, it decides per field: a SINGLE value becomes a parent column, an OBJECT gets flattened into prefixed parent columns, an ARRAY becomes rows in a separate child table. This is the one class doing the real "relational decomposition" work.

**writer/RowWriter**
Takes parent rows produced by SchemaWalker and inserts them into the main table, batching multiple rows together for performance rather than inserting one at a time.

**writer/ChildTableWriter**
Takes child rows produced by SchemaWalker, makes sure the relevant child table exists (creating it if needed), and inserts the rows with a foreign key pointing back to their parent.

**config/ReadJSON**
Deserializes `mappings.json` into the config objects the rest of the app uses — source/destination connection info, table name, and the list of field mappings.

**config/ConfigGenerator**
A separate CLI mode, not part of the normal run path. Samples real documents from a collection, reuses the same shape-detection idea as SchemaWalker, and writes out a starter `mappings.json` for a person to review and edit by hand. It never drives a migration directly.

**log/RunLogger**
Writes a row to `migration_run_log` at the start of a run, updates it with final counts and status when the run finishes (success, partial, or failed), and records individual row-level errors so a failed document doesn't just disappear silently.

---

## Notes worth keeping in mind as you build

- Getting a real generated id back from a batched parent insert (so child tables can reference it) is genuine work — don't assume it falls out for free from a basic batch insert.
- Child-table inserts can start out one row at a time; batching those is reasonable to defer until parent-row batching is already working and tested.
- `ConfigGenerator` depends on `SchemaWalker`'s shape-detection logic existing and being correct first — build it after, not in parallel.
- Every connector and writer should be safe to call `close()` on even if the run failed partway through — that's what makes `App`'s cleanup step actually reliable.