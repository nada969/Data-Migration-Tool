# Data Migration Tool - Core Project Idea

## Vision

Create a **robust, configurable, and automated data migration tool** that seamlessly transfers data from MongoDB (NoSQL) to PostgreSQL (SQL), handling schema transformation, type conversion, and data validation automatically.

---

## The Problem

### Business Context
Organizations often need to migrate data between different database systems for various reasons:
- **Technology Stack Changes**: Moving from NoSQL to SQL for better ACID compliance
- **Analytics Requirements**: SQL databases offer better support for complex queries and reporting
- **Data Consolidation**: Merging data from multiple sources into a unified SQL database
- **Compliance**: Regulatory requirements may mandate structured, relational data storage

### Technical Challenges
1. **Schema Mismatch**: MongoDB uses flexible, document-based schema while PostgreSQL requires structured tables
2. **Data Type Differences**: Different type systems between NoSQL and SQL
3. **Manual Migration**: Time-consuming, error-prone, and difficult to repeat
4. **Data Volume**: Large datasets require efficient batch processing
5. **Data Integrity**: Ensuring no data loss during migration

---

## The Solution

### Core Concept
A **Java-based ETL (Extract, Transform, Load) tool** that:
- **Extracts** data from MongoDB collections
- **Transforms** documents into relational rows with proper type conversion
- **Loads** data into PostgreSQL tables

### Key Innovation
**Configuration-Driven Migration**: Instead of writing custom migration scripts for each use case, users define migrations through simple JSON configuration files.

---

## Architecture Philosophy

### Design Principles

#### 1. **Separation of Concerns**
```
MongoDB Handler    ←→    Migration Logic    ←→    PostgreSQL Handler
(Data Source)           (Transformation)          (Data Destination)
```

Each component has a single responsibility:
- `MongoDB.java`: Only handles MongoDB connections and data retrieval
- `PostgreSQL.java`: Only handles PostgreSQL connections and data insertion
- `App.java` / `App2.java`: Orchestrates the migration process
- `ReadJSON.java`: Handles configuration parsing

#### 2. **Flexibility Through Configuration**
Users can control:
- Which collections to migrate
- How fields map between MongoDB and PostgreSQL
- Data type conversions
- Connection parameters
- Scheduling (future feature)

#### 3. **Type Safety**
Automatic type detection and conversion:
```
MongoDB Type          →    PostgreSQL Type
─────────────────────────────────────────
String                →    VARCHAR/TEXT
Integer               →    INTEGER
Long                  →    BIGINT
Double                →    DOUBLE PRECISION
Boolean               →    BOOLEAN
Date                  →    TIMESTAMP
null                  →    NULL
ObjectId              →    VARCHAR
```

---

## Migration Approaches

### Approach 1: Automatic Full Migration (App.java)
**Use Case**: Quick, one-time migration of entire database

**How it works**:
```
1. Connect to MongoDB
2. List all collections
3. For each collection:
   a. Read all documents
   b. For each document:
      - Extract all fields
      - Detect data types
      - Build INSERT statement
      - Execute insertion
```

**Pros**:
- Simple to use
- No configuration needed
- Fast for small databases

**Cons**:
- No field mapping control
- Migrates everything (no filtering)
- Table must already exist in PostgreSQL

---

### Approach 2: Configuration-Based Migration (App2.java)
**Use Case**: Production environments, selective migration, scheduled migrations

**How it works**:
```
1. Read conf.json configuration
2. Connect to specified MongoDB database/collection
3. Connect to specified PostgreSQL database/table
4. Apply field mappings from configuration
5. Migrate data according to rules
6. (Future) Run on schedule
```

**Pros**:
- Full control over migration
- Field-level mapping
- Reusable configurations
- Production-ready

**Cons**:
- Requires configuration setup
- More complex initial setup

---

## Configuration Schema

### Example Configuration (`conf.json`)
```json
{
  "source": "mongoDB",
  "urlSource": "mongodb://localhost:27017",
  "destination": "postgres",
  "urlDestination": "jdbc:postgresql://localhost:5432/postgres",
  "destinationPassWord": "password",
  "collectionName": "products",
  "tableName": "products",
  "cronSchedule": "0 3 * * *",
  "mappings": [
    {
      "mongoKey": "_id",
      "psqlCol": "product_id",
      "type": "String"
    },
    {
      "mongoKey": "name",
      "psqlCol": "product_name",
      "type": "String"
    },
    {
      "mongoKey": "price",
      "psqlCol": "price",
      "type": "Double"
    }
  ]
}
```

### Configuration Elements

| Field | Purpose | Example |
|-------|---------|---------|
| `source` | Source database name | "mongoDB" |
| `urlSource` | MongoDB connection URL | "mongodb://localhost:27017" |
| `destination` | Destination database type | "postgres" |
| `urlDestination` | PostgreSQL JDBC URL | "jdbc:postgresql://localhost:5432/db" |
| `collectionName` | MongoDB collection to migrate | "users" |
| `tableName` | PostgreSQL table to insert into | "users" |
| `cronSchedule` | Schedule for automated runs | "0 3 * * *" (3 AM daily) |
| `mappings` | Field-level mapping rules | Array of mapping objects |

---

## Technical Implementation

### Data Flow

```mermaid
graph LR
    A[MongoDB Collection] --> B[Read Documents]
    B --> C[Extract Fields]
    C --> D[Type Detection]
    D --> E[Build SQL Statement]
    E --> F[PreparedStatement]
    F --> G[PostgreSQL Table]
    
    style A fill:#47A248
    style G fill:#336791
    style D fill:#FFA500
```

### Type Conversion Logic

```java
// Intelligent type handling
if (value == null) {
    preparedStatement.setNull(index, java.sql.Types.NULL);
} else if (value instanceof Integer) {
    preparedStatement.setInt(index, (Integer) value);
} else if (value instanceof Double) {
    preparedStatement.setDouble(index, (Double) value);
} else if (value instanceof Date) {
    preparedStatement.setTimestamp(index, 
        new java.sql.Timestamp(((Date) value).getTime()));
} else {
    preparedStatement.setString(index, value.toString());
}
```

### SQL Generation

```java
// Dynamic SQL generation
String columns = "_id, name, price";
String placeholders = "?, ?, ?";
String sql = "INSERT INTO products (" + columns + ") VALUES (" + placeholders + ")";

// Result: INSERT INTO products (_id, name, price) VALUES (?, ?, ?)
```

---

## Use Cases & Scenarios

### 1. E-Commerce Platform Migration
**Scenario**: Migrating product catalog from MongoDB to PostgreSQL for better reporting

**Before** (MongoDB):
```json
{
  "_id": "prod_123",
  "name": "Laptop",
  "price": 999.99,
  "inventory": 50,
  "tags": ["electronics", "computers"]
}
```

**After** (PostgreSQL):
```sql
product_id | product_name | price  | inventory
-----------|--------------|--------|----------
prod_123   | Laptop       | 999.99 | 50
```

### 2. User Data Consolidation
**Scenario**: Consolidating user data from multiple MongoDB instances into a single PostgreSQL database

### 3. Analytics & Business Intelligence
**Scenario**: Moving operational data to PostgreSQL for SQL-based analytics tools (Tableau, Power BI)

### 4. Compliance & Auditing
**Scenario**: Creating structured audit logs in PostgreSQL from MongoDB application logs

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 25 |
| Build Tool | Maven | - |
| MongoDB Driver | mongodb-driver-sync | 5.5.1 |
| PostgreSQL Driver | postgresql | 42.7.8 |
| JSON Parser | Jackson | 2.17.1 |
| Code Generation | Lombok | Latest |
| Testing | JUnit | 3.8.1 |

---

## Core Philosophy

> **"Automate the tedious, simplify the complex, and make data migration accessible to everyone."**

This tool embodies the principle that **data should flow freely** between systems without manual intervention, enabling organizations to choose the best database for each use case without being locked into a single technology.
