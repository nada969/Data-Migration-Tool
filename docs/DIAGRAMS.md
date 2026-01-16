# System Diagrams - Data Migration Tool

This document contains all the architectural and design diagrams for the Data Migration Tool project.

---

## 1. Sequence Diagram - Migration Flow

### App.java (Automatic Migration)

```mermaid
sequenceDiagram
    participant User
    participant App
    participant MongoDB
    participant PostgreSQL
    
    User->>App: Run App.main()
    App->>MongoDB: connect()
    MongoDB-->>App: Connection established
    
    App->>PostgreSQL: connect(url, user, password)
    PostgreSQL-->>App: Connection established
    
    App->>MongoDB: getDatabase("mongoDB")
    MongoDB-->>App: Database instance
    
    App->>MongoDB: listCollectionNames()
    MongoDB-->>App: [collection1, collection2, ...]
    
    loop For each collection
        App->>MongoDB: getCollection(collectionName)
        MongoDB-->>App: Collection instance
        
        App->>MongoDB: collection.find()
        MongoDB-->>App: [doc1, doc2, doc3, ...]
        
        loop For each document
            App->>App: Extract fields (keySet())
            App->>App: Build HashMap<String, Object>
            App->>App: Detect data types
            App->>App: Generate SQL INSERT statement
            App->>App: Create PreparedStatement
            
            loop For each field
                App->>App: Set parameter based on type
                Note over App: setInt(), setString(), setDouble(), etc.
            end
            
            App->>PostgreSQL: executeUpdate()
            
            alt Success
                PostgreSQL-->>App: Rows affected
                App->>User: Log: "Inserted successfully!"
            else Error
                PostgreSQL-->>App: SQLException
                App->>User: Log: "Insertion failed"
            end
        end
    end
    
    Note over App,PostgreSQL: Connections remain open
```

### App2.java (Configuration-Based Migration)

```mermaid
sequenceDiagram
    participant User
    participant App2
    participant ReadJSON
    participant MongoDB
    participant PostgreSQL
    
    User->>App2: Run App2.main()
    App2->>ReadJSON: Read conf.json
    ReadJSON-->>App2: Configuration object
    
    App2->>MongoDB: connect()
    MongoDB-->>App2: Connection established
    
    App2->>PostgreSQL: connect(url, user, password)
    Note over App2,PostgreSQL: Uses config values
    PostgreSQL-->>App2: Connection established
    
    App2->>MongoDB: getDatabase(source)
    MongoDB-->>App2: Database instance
    
    App2->>MongoDB: getCollection(collectionName)
    Note over App2,MongoDB: From configuration
    MongoDB-->>App2: Collection instance
    
    App2->>MongoDB: collection.find()
    MongoDB-->>App2: Documents
    
    loop For each document
        App2->>App2: Extract fields using mappings
        App2->>App2: Apply field transformations
        App2->>App2: Build SQL INSERT
        
        App2->>PostgreSQL: Check if table exists
        
        alt Table exists
            App2->>PostgreSQL: executeUpdate(INSERT)
            PostgreSQL-->>App2: Success/Failure
        else Table doesn't exist
            App2->>PostgreSQL: CREATE TABLE
            PostgreSQL-->>App2: Table created
            App2->>PostgreSQL: executeUpdate(INSERT)
            PostgreSQL-->>App2: Success/Failure
        end
    end
```

---

## 2. Class Diagram

```mermaid
classDiagram
    class App {
        +main(String[] args) void
    }
    
    class App2 {
        +main(String[] args) void
    }
    
    class MongoDB {
        +MongoClient mongoClient
        +HashMap~String,String~ hashMap
        +connect() void
        +exportCollection() void
        +close() void
        +main(String[] args) void
    }
    
    class PostgreSQL {
        +Connection conn
        +connect(String url, String user, String password) void
        +close() void
    }
    
    class ReadJSON {
        -String source
        -String urlSource
        -String destination
        -String urlDestination
        -String destinationPassWord
        -String collectionName
        -String tableName
        -String cronSchedule
        -List~Mapping~ mappings
        +getSource() String
        +getUrlSource() String
        +getDestination() String
        +getUrlDestination() String
        +getDestinationPassWord() String
        +getCollectionName() String
        +getTableName() String
        +getCronSchedule() String
        +getMappings() List~Mapping~
    }
    
    class Mapping {
        -String mongoKey
        -String psqlCol
        -String type
        +getMongoKey() String
        +getPsqlCol() String
        +getType() String
    }
    
    class intType {
        <<interface>>
    }
    
    class javaSQL {
        <<interface>>
    }
    
    class nullType {
        <<interface>>
    }
    
    App ..> MongoDB : uses
    App ..> PostgreSQL : uses
    App2 ..> MongoDB : uses
    App2 ..> PostgreSQL : uses
    App2 ..> ReadJSON : uses
    ReadJSON *-- Mapping : contains
    
    note for App "Simple automatic migration\nMigrates all collections"
    note for App2 "Configuration-based migration\nUses conf.json for settings"
    note for MongoDB "Handles MongoDB connections\nand data retrieval"
    note for PostgreSQL "Handles PostgreSQL connections\nand data insertion"
    note for ReadJSON "Parses configuration file\nContains migration settings"
```

---

## 3. Component Diagram

```mermaid
graph TB
    subgraph "Data Migration Tool"
        subgraph "Application Layer"
            App[App.java<br/>Automatic Migration]
            App2[App2.java<br/>Config-Based Migration]
        end
        
        subgraph "Configuration Layer"
            Config[conf.json<br/>Configuration File]
            ReadJSON[ReadJSON.java<br/>Config Parser]
        end
        
        subgraph "Database Handlers"
            MongoHandler[MongoDB.java<br/>MongoDB Handler]
            PgHandler[PostgreSQL.java<br/>PostgreSQL Handler]
        end
        
        subgraph "Utilities"
            IntType[intType.java]
            JavaSQL[javaSQL.java]
            NullType[nullType.java]
        end
    end
    
    subgraph "External Systems"
        MongoDBServer[(MongoDB<br/>Source Database)]
        PostgreSQLServer[(PostgreSQL<br/>Destination Database)]
    end
    
    App --> MongoHandler
    App --> PgHandler
    
    App2 --> ReadJSON
    App2 --> MongoHandler
    App2 --> PgHandler
    
    ReadJSON --> Config
    
    MongoHandler --> MongoDBServer
    PgHandler --> PostgreSQLServer
    
    App2 -.-> IntType
    App2 -.-> JavaSQL
    App2 -.-> NullType
    
    style App fill:#90EE90
    style App2 fill:#87CEEB
    style MongoHandler fill:#47A248,color:#fff
    style PgHandler fill:#336791,color:#fff
    style MongoDBServer fill:#47A248,color:#fff
    style PostgreSQLServer fill:#336791,color:#fff
    style Config fill:#FFD700
```

---

## 4. Architecture Diagram - Data Flow

```mermaid
graph LR
    subgraph "Source: MongoDB"
        DB1[(MongoDB Database)]
        C1[Collection 1]
        C2[Collection 2]
        C3[Collection N]
        
        DB1 --> C1
        DB1 --> C2
        DB1 --> C3
    end
    
    subgraph "Migration Tool"
        direction TB
        Read[1. Read Documents]
        Extract[2. Extract Fields]
        Transform[3. Type Conversion]
        Map[4. Field Mapping]
        Generate[5. Generate SQL]
        Execute[6. Execute INSERT]
        
        Read --> Extract
        Extract --> Transform
        Transform --> Map
        Map --> Generate
        Generate --> Execute
    end
    
    subgraph "Destination: PostgreSQL"
        DB2[(PostgreSQL Database)]
        T1[Table 1]
        T2[Table 2]
        T3[Table N]
        
        DB2 --> T1
        DB2 --> T2
        DB2 --> T3
    end
    
    C1 --> Read
    C2 --> Read
    C3 --> Read
    
    Execute --> T1
    Execute --> T2
    Execute --> T3
    
    style DB1 fill:#47A248,color:#fff
    style DB2 fill:#336791,color:#fff
    style Transform fill:#FFA500
    style Map fill:#FF6347
```

---

## 5. ERD (Entity Relationship Diagram)

### MongoDB Schema (Document Model)

```mermaid
erDiagram
    MONGODB_DATABASE ||--o{ COLLECTION : contains
    COLLECTION ||--o{ DOCUMENT : contains
    DOCUMENT ||--o{ FIELD : has
    
    MONGODB_DATABASE {
        string name
        string connection_url
    }
    
    COLLECTION {
        string collection_name
        int document_count
    }
    
    DOCUMENT {
        ObjectId _id
        json data
    }
    
    FIELD {
        string key
        any value
        string type
    }
```

### PostgreSQL Schema (Relational Model)

```mermaid
erDiagram
    POSTGRESQL_DATABASE ||--o{ TABLE : contains
    TABLE ||--o{ COLUMN : has
    TABLE ||--o{ ROW : contains
    ROW ||--o{ CELL : has
    
    POSTGRESQL_DATABASE {
        string name
        string jdbc_url
        string username
        string password
    }
    
    TABLE {
        string table_name
        int row_count
        string schema
    }
    
    COLUMN {
        string column_name
        string data_type
        boolean nullable
        string constraints
    }
    
    ROW {
        int row_id
    }
    
    CELL {
        string column_name
        any value
    }
```



### Migration Mapping

```mermaid
erDiagram
    CONFIGURATION ||--o{ MAPPING : contains
    MAPPING ||--|| MONGO_FIELD : maps_from
    MAPPING ||--|| PSQL_COLUMN : maps_to
    
    CONFIGURATION {
        string source_db
        string source_url
        string dest_db
        string dest_url
        string collection_name
        string table_name
        string cron_schedule
    }
    
    MAPPING {
        string mongo_key
        string psql_col
        string type
    }
    
    MONGO_FIELD {
        string field_name
        string mongo_type
    }
    
    PSQL_COLUMN {
        string column_name
        string sql_type
    }
```

---

## 6. State Diagram - Migration Process

```mermaid
stateDiagram-v2
    [*] --> Initialized
    
    Initialized --> ConnectingMongoDB : Start Migration
    
    ConnectingMongoDB --> ConnectingPostgreSQL : MongoDB Connected
    ConnectingMongoDB --> Error : Connection Failed
    
    ConnectingPostgreSQL --> ReadingConfiguration : PostgreSQL Connected
    ConnectingPostgreSQL --> Error : Connection Failed
    
    ReadingConfiguration --> FetchingCollections : Config Loaded
    ReadingConfiguration --> FetchingCollections : No Config (App.java)
    
    FetchingCollections --> ProcessingDocuments : Collections Retrieved
    FetchingCollections --> Error : No Collections Found
    
    ProcessingDocuments --> ExtractingFields : Document Found
    ProcessingDocuments --> Completed : No More Documents
    
    ExtractingFields --> ConvertingTypes : Fields Extracted
    
    ConvertingTypes --> BuildingSQL : Types Converted
    
    BuildingSQL --> ExecutingInsert : SQL Generated
    
    ExecutingInsert --> ProcessingDocuments : Insert Successful
    ExecutingInsert --> LoggingError : Insert Failed
    
    LoggingError --> ProcessingDocuments : Continue with Next
    
    Completed --> [*]
    Error --> [*]
    
    note right of ConnectingMongoDB
        Connects to MongoDB
        using connection URL
    end note
    
    note right of ConvertingTypes
        Integer → INT
        String → VARCHAR
        Double → DOUBLE
        Date → TIMESTAMP
    end note
```

---

## 7. Deployment Diagram

```mermaid
graph TB
    subgraph "Development Environment"
        Dev[Developer Machine]
        IDE[IntelliJ IDEA]
        Maven[Maven Build Tool]
        
        Dev --> IDE
        IDE --> Maven
    end
    
    subgraph "Application Server"
        JVM[Java Virtual Machine<br/>Java 25]
        App[Data Migration Tool<br/>JAR File]
        Config[conf.json]
        Logs[Log Files]
        
        JVM --> App
        App --> Config
        App --> Logs
    end
    
    subgraph "Database Servers"
        MongoServer[MongoDB Server<br/>Port 27017]
        PgServer[PostgreSQL Server<br/>Port 5432]
    end
    
    Maven -->|Build| App
    
    App -->|JDBC Connection| MongoServer
    App -->|JDBC Connection| PgServer
    
    style Dev fill:#E8E8E8
    style JVM fill:#FF6B6B
    style MongoServer fill:#47A248,color:#fff
    style PgServer fill:#336791,color:#fff
```

---

## 8. Activity Diagram - Complete Migration Workflow

```mermaid
flowchart TD
    Start([Start Migration]) --> LoadConfig{Configuration<br/>Required?}
    
    LoadConfig -->|Yes - App2| ReadConfig[Read conf.json]
    LoadConfig -->|No - App| ConnectMongo
    
    ReadConfig --> ValidateConfig{Valid<br/>Config?}
    ValidateConfig -->|No| Error1[Log Error]
    ValidateConfig -->|Yes| ConnectMongo
    
    ConnectMongo[Connect to MongoDB] --> MongoSuccess{Connected?}
    MongoSuccess -->|No| Error2[Log Connection Error]
    MongoSuccess -->|Yes| ConnectPg
    
    ConnectPg[Connect to PostgreSQL] --> PgSuccess{Connected?}
    PgSuccess -->|No| Error3[Log Connection Error]
    PgSuccess -->|Yes| GetCollections
    
    GetCollections[Get Collections List] --> HasCollections{Collections<br/>Found?}
    HasCollections -->|No| Error4[No Data to Migrate]
    HasCollections -->|Yes| LoopCollections
    
    LoopCollections{More<br/>Collections?} -->|Yes| GetDocs[Get Documents from Collection]
    LoopCollections -->|No| CloseConnections
    
    GetDocs --> LoopDocs{More<br/>Documents?}
    LoopDocs -->|Yes| ExtractFields[Extract Document Fields]
    LoopDocs -->|No| LoopCollections
    
    ExtractFields --> CreateHashMap[Create HashMap<String, Object>]
    CreateHashMap --> DetectTypes[Detect Data Types]
    DetectTypes --> BuildSQL[Build SQL INSERT Statement]
    BuildSQL --> CreatePrepared[Create PreparedStatement]
    CreatePrepared --> SetParams[Set Parameters by Type]
    
    SetParams --> Execute[Execute INSERT]
    Execute --> Success{Successful?}
    
    Success -->|Yes| LogSuccess[Log: Inserted Successfully]
    Success -->|No| LogFailure[Log: Insertion Failed]
    
    LogSuccess --> LoopDocs
    LogFailure --> LoopDocs
    
    CloseConnections[Close Database Connections] --> End([End Migration])
    
    Error1 --> End
    Error2 --> End
    Error3 --> End
    Error4 --> End
    
    style Start fill:#90EE90
    style End fill:#FFB6C1
    style Error1 fill:#FF6347,color:#fff
    style Error2 fill:#FF6347,color:#fff
    style Error3 fill:#FF6347,color:#fff
    style Error4 fill:#FF6347,color:#fff
    style Execute fill:#4169E1,color:#fff
    style LogSuccess fill:#32CD32,color:#fff
    style LogFailure fill:#FFA500
```

---

## 9. Type Conversion Matrix

```mermaid
graph LR
    subgraph "MongoDB Types"
        M1[String]
        M2[Integer]
        M3[Long]
        M4[Double]
        M5[Boolean]
        M6[Date]
        M7[ObjectId]
        M8[null]
    end
    
    subgraph "Java Processing"
        J[Type Detection<br/>& Conversion]
    end
    
    subgraph "PostgreSQL Types"
        P1[VARCHAR/TEXT]
        P2[INTEGER]
        P3[BIGINT]
        P4[DOUBLE PRECISION]
        P5[BOOLEAN]
        P6[TIMESTAMP]
        P7[VARCHAR]
        P8[NULL]
    end
    
    M1 --> J --> P1
    M2 --> J --> P2
    M3 --> J --> P3
    M4 --> J --> P4
    M5 --> J --> P5
    M6 --> J --> P6
    M7 --> J --> P7
    M8 --> J --> P8
    
    style J fill:#FFA500
```

---

## Diagram Usage Guide

### For Developers
- **Sequence Diagrams**: Understand the flow of method calls
- **Class Diagram**: See the structure and relationships
- **Activity Diagram**: Follow the complete workflow logic

### For Architects
- **Component Diagram**: Understand system modules
- **Architecture Diagram**: See data flow between systems
- **Deployment Diagram**: Plan infrastructure

### For Database Administrators
- **ERD Diagrams**: Understand data models
- **Type Conversion Matrix**: See how data types map

### For Project Managers
- **State Diagram**: Track migration states
- **Activity Diagram**: Understand the process flow
