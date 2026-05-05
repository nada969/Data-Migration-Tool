# Data Migration Tool — TODO

## Project Overview
A Java-based ETL tool to migrate data from MongoDB (NoSQL) to PostgreSQL (SQL) with automatic type conversion, configurable field mappings, observability, scheduling, and containerized deployment.

Built with core Java + libraries (MongoDB driver, JDBC, Jackson, HikariCP, SLF4J).  
Spring Boot is an optional future enhancement.

---

## Completed

### Core Functionality
- [x] MongoDB connection handler (`MongoDB.java`)
- [x] PostgreSQL connection handler (`PostgreSQL.java`)
- [x] Basic migration application (`App.java`)
- [x] Configuration-based migration (`App2.java`)
- [x] JSON configuration parser (`ReadJSON.java`)
- [x] Automatic type conversion (Integer, Long, Double, Boolean, Date, String, null)
- [x] Dynamic SQL INSERT generation
- [x] PreparedStatement (SQL injection prevention)
- [x] Basic error handling and logging

### Configuration
- [x] JSON config file structure (`conf.json`)
- [x] Database connection parameters
- [x] Field mapping configuration
- [x] Collection → Table name mapping

### Documentation
- [x] Project explanation document
- [x] System architecture diagrams
- [x] Sequence diagrams
- [x] Class diagrams
- [x] ERD diagrams

---

## In Progress

### App2 Fixes
- [ ] Fix commented-out code in `App2.java` (lines 77–127)
- [ ] Complete table existence check logic
- [ ] Implement automatic table creation if not exists
- [ ] Fix data insertion logic in exception handler

---

## Planned

### High Priority

#### 1. Enhanced Error Handling
- [ ] Detailed error logging with timestamps
- [ ] Retry logic for failed insertions (exponential backoff)
- [ ] Config file validation on startup
- [ ] Graceful connection timeout handling
- [ ] Rollback mechanism for failed batches

#### 2. Table Management
- [ ] Auto-create PostgreSQL tables from MongoDB schema
- [ ] Column type inference from document fields
- [ ] Primary key and index creation
- [ ] Handle schema changes and column additions

#### 3. Data Validation
- [ ] Pre-insertion data validation
- [ ] Duplicate record detection
- [ ] Type mismatch handling (skip / log / fail policy)
- [ ] Configurable transformation rules per field

#### 4. Performance Optimization
- [ ] Batch inserts via `PreparedStatement.executeBatch()` (target: 500 rows/batch)
- [ ] HikariCP connection pooling (`maximumPoolSize` tied to thread count)
- [ ] Cursor-based MongoDB streaming (never load full collection into RAM)
- [ ] Parallel processing with `ThreadPoolExecutor` (one thread per collection)
- [ ] Progress tracking and throughput reporting

#### 5. Advanced Mapping
- [ ] Nested document → JSONB mapping
- [ ] Array / List field handling (`TEXT[]` or JSONB)
- [ ] Custom field transformation functions (rename, cast, drop)
- [ ] MongoDB DBRef / embedded document support

---

### Medium Priority

#### 6. Observability — Logging
- [ ] Integrate SLF4J + Logback (replace `System.out`)
- [ ] Structured log output (JSON format for ELK / Grafana Loki ingestion)
- [ ] Log levels per stage:
  - `INFO` — config loaded, batch committed, run summary
  - `WARN` — skipped record (with document `_id`)
  - `ERROR` — SQL exception, connection failure (with stack trace)
- [ ] Per-batch log line: batch #, row count, elapsed ms
- [ ] Log rotation via `logback.xml` (max 10 files × 50 MB)

#### 7. Observability — Metrics
- [ ] `MigrationMetrics` class with `AtomicLong` counters:
  - `records_processed`, `error_count`, `skipped_count`
  - `total_read_ms`, `total_transform_ms`, `total_write_ms`
- [ ] `StageTimer` — try-with-resources per-stage timer
- [ ] End-of-run summary printed to log and written to `migration-report.json`
- [ ] Optional: expose counters via Micrometer → Prometheus endpoint
- [ ] Latency benchmark report: read vs transform vs write breakdown

#### 8. Scheduling — Cron Job
- [ ] Linux cron support (run as CLI jar, log to file):
  ```
  0 2 * * * java -jar /opt/etl/etl-tool.jar /opt/etl/conf.json >> /var/log/etl.log 2>&1
  ```
- [ ] Java `ScheduledExecutorService` for in-process scheduling (daemon mode)
- [ ] Quartz Scheduler integration for cron-expression config (`cronSchedule` field in `conf.json`)
- [ ] Incremental migration: store last migrated `_id` / timestamp to resume without full re-run
- [ ] Migration history table in PostgreSQL (`etl_run_log`)

#### 9. Configuration Enhancements
- [ ] Multiple collections in one config file
- [ ] Environment variable resolution (`${VAR_NAME}` in JSON values)
- [ ] Environment profiles: `conf-dev.json`, `conf-prod.json`
- [ ] Encrypted password support (or external secret reference)
- [ ] `onError` policy per mapping (`SKIP` / `FAIL` / `LOG`)

---

### CI/CD Pipeline

#### 10. GitHub Actions
- [ ] Workflow file: `.github/workflows/ci.yml`
- [ ] Trigger: push to `main` + all pull requests
- [ ] Steps:
  - [ ] Checkout code
  - [ ] Set up JDK 21 (Temurin) with Maven cache
  - [ ] `mvn -B verify` (compile → unit tests → integration tests)
  - [ ] Fail pipeline on any test error (non-zero exit)
  - [ ] Upload test report as artifact
- [ ] Docker build step (main branch only):
  - [ ] `docker build -t etl-tool:${{ github.sha }} .`
  - [ ] Push image to GitHub Container Registry (`ghcr.io`)
- [ ] Badge in `README.md` showing CI status

---

### Dockerization

#### 11. Dockerfile
- [ ] Multi-stage build:
  - Stage 1 (`builder`): `maven:3.9-eclipse-temurin-21` — compile and package
  - Stage 2 (`runtime`): `eclipse-temurin:21-jre-alpine` — slim runtime only
- [ ] Non-root user (`etl`) for security
- [ ] Config mounted as volume at runtime (never baked into image):
  ```bash
  docker run --rm \
    -v $(pwd)/conf.json:/config/conf.json:ro \
    -e ETL_PG_PASSWORD=secret \
    --network host \
    etl-tool
  ```
- [ ] `.dockerignore` to exclude `target/`, `.idea/`, test resources
- [ ] `docker-compose.yml` for local dev (MongoDB + PostgreSQL + ETL tool):
  - Postgres health-check → ETL waits until DB is ready
  - Named volume for Postgres data persistence

---

### Deployment & Live Testing

#### 12. Live Server Deployment
- [ ] **Target server**: Oracle Cloud Free Tier (always-free ARM VM — 4 OCPUs, 24 GB RAM)
  - Alternative: Fly.io free tier (Docker-native, great CLI)
- [ ] Provision steps:
  - [ ] Install Docker on VPS (`apt-get install docker.io`)
  - [ ] Run PostgreSQL container (`postgres:16`)
  - [ ] Run MongoDB container (`mongo:7`)
  - [ ] Pull and run ETL image from `ghcr.io`
- [ ] Live latency benchmark:
  - [ ] Seed MongoDB with 10K, 100K, 500K documents
  - [ ] Record: `read_ms`, `transform_ms`, `write_ms` per run
  - [ ] Compare row-by-row vs batch insert throughput
  - [ ] Log results to `benchmark-report.json`
- [ ] Production best practices:
  - [ ] Secrets via environment variables (never in image)
  - [ ] Drop non-essential Postgres indexes before migration, rebuild after
  - [ ] `reWriteBatchedInserts=true` in JDBC URL for Postgres
  - [ ] Set `ulimits` on Docker containers for file descriptors

---

### Lower Priority

#### 13. Testing
- [ ] Unit tests — `TypeMapper`: ObjectId→TEXT, nested Document→JSONB, null handling, ISO date→TIMESTAMPTZ
- [ ] Unit tests — `ConfigLoader`: valid config, missing env var, malformed JSON
- [ ] Unit tests — `BatchPostgresWriter`: batch size boundary, rollback on failure
- [ ] Integration tests — full Mongo→Postgres pipeline using **Testcontainers**
  - Spins up real Docker Mongo + Postgres per test run
  - Asserts row count, field values, JSONB structure
- [ ] Performance / load test: 1M document migration, assert throughput > 20K rows/sec
- [ ] Test coverage report via JaCoCo (`mvn verify` generates HTML report)

#### 14. Documentation
- [ ] API documentation (Javadoc on all public classes)
- [ ] User guide: config file reference + field mapping examples
- [ ] Deployment guide (Docker + VPS steps)
- [ ] Troubleshooting guide (common errors + fixes)
- [ ] `CONTRIBUTING.md`

#### 15. CLI & UI
- [ ] CLI argument parsing (`--config`, `--dry-run`, `--collection`)
- [ ] Dry-run mode: validate config + count documents, no writes
- [ ] Migration rollback command
- [ ] Interactive configuration wizard
- [ ] Web-based admin panel (low priority)

#### 16. Additional Database Support
- [ ] MySQL and Oracle JDBC support
- [ ] Bi-directional sync (PostgreSQL → MongoDB)
- [ ] Cloud databases: AWS RDS, MongoDB Atlas
- [ ] Other NoSQL sources: Cassandra, CouchDB

---

## Future Enhancements

- [ ] Document filtering (migrate only matching documents via query)
- [ ] Data masking / anonymization for PII fields
- [ ] Migration templates for common schemas
- [ ] Plugin system for custom field transformers
- [ ] REST API for remote migration triggering
- [ ] Real-time streaming migration (MongoDB Change Streams → Postgres)

---

## Spring Boot (Optional — Future)

Not required. May be introduced later for modularity and config management.

- [ ] Spring Boot application structure + DI for DB services
- [ ] Externalized config via `application.yml`
- [ ] Profile-based environments (`dev` / `staging` / `prod`)
- [ ] Spring Scheduler / Quartz integration
- [ ] Spring Data MongoDB + JPA (optional)
- [ ] REST API for triggering and monitoring migrations
- [ ] Actuator health checks + metrics endpoints
