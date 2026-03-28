# Spring Batch Phone Scoring POC

A Spring Batch application that scores phone numbers for debt recovery campaigns, dispatches sorted call lists to a dialer, and exposes AI-generated negotiation cheat sheets for phone agents.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Quick Start (Local Development)](#quick-start-local-development)
- [Running the Application](#running-the-application)
- [Testing the APIs](#testing-the-apis)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                     Spring Batch Job (daily 05:00)                   │
│                                                                      │
│  ┌──────────┐   ┌─────────────┐   ┌──────────────┐   ┌───────────┐ │
│  │ API      │   │ DB Replica  │   │ S3 CSV       │   │           │ │
│  │ Reader   │──▸│ Reader      │──▸│ Reader       │──▸│ Merge +   │ │
│  │ (loans,  │   │ (PTP, last  │   │ (answer      │   │ Score     │ │
│  │ balance, │   │  contact)   │   │  rates)      │   │ Compute   │ │
│  │ app)     │   └─────────────┘   └──────────────┘   └─────┬─────┘ │
│  └──────────┘                                               │       │
│                                                             ▼       │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                  Feature Store (PostgreSQL)                     │ │
│  │  phone_number | scores | features | audit timestamp            │ │
│  └───────────────────────────────┬─────────────────────────────────┘ │
│                                  │                                   │
│                    ┌─────────────┴─────────────┐                     │
│                    ▼                           ▼                     │
│         ┌──────────────────┐       ┌──────────────────────┐         │
│         │ Dialer Dispatch  │       │ Cheat Sheet Endpoint │         │
│         │ (Five9 / AWS     │       │ (LLM via Bedrock     │         │
│         │  Connect)        │       │  or Mock)            │         │
│         └──────────────────┘       └──────────────────────┘         │
└──────────────────────────────────────────────────────────────────────┘
```

### Key Features

| Feature | Description |
|---------|-------------|
| **3-source data ingestion** | REST API, database read-replica, S3 CSV files (all mocked for demo) |
| **Dual scoring** | **Reach Score** (likelihood of answering) and **Recovery Score** (likelihood of repaying) |
| **Feature store** | PostgreSQL table with full audit trail per phone number per day |
| **Dialer strategy pattern** | Swap between Five9 and AWS Connect with a config change—no code changes |
| **AI cheat sheets** | AWS Bedrock / mock LLM generates tailored negotiation advice per contact |
| **Swagger UI** | Auto-generated API docs at `/swagger-ui.html` |

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| **Java** | 17+ | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **Docker** | 20+ | `docker --version` – needed for local PostgreSQL |
| **Docker Compose** | v2+ | Usually bundled with Docker Desktop |

> **No AWS credentials needed for local development.** The `local` profile uses mock data sources and a mock LLM provider.

---

## Quick Start (Local Development)

```bash
# 1. Clone the repository
git clone https://github.com/edg7007-netizen/poc_spring_batch.git
cd poc_spring_batch

# 2. Start the app (Docker Compose auto-starts PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. Wait for the app to start, then trigger the batch job
curl -X POST http://localhost:8080/api/v1/batch/run

# 4. Query the feature store
curl http://localhost:8080/api/v1/batch/feature-store

# 5. Get a cheat sheet for a phone number
curl http://localhost:8080/api/v1/cheat-sheet/by-phone/5551001001

# 6. Open Swagger UI in your browser
open http://localhost:8080/swagger-ui.html
```

### What happens under the hood

1. **`spring-boot-docker-compose`** detects `compose.yml` and automatically starts a PostgreSQL 16 container.
2. **Flyway** runs the migration `V1__create_phone_feature_store.sql` to create the feature store table.
3. The batch job is **not** auto-triggered on startup (the cron schedule is `0 0 5 * * ?`). Use the manual trigger endpoint `POST /api/v1/batch/run`.
4. Mock data is loaded from the three readers (API, DB, S3) and scored.
5. Results are written to the `phone_feature_store` table.
6. The sorted phone list is dispatched to the Five9 dialer (mocked).

---

## Running the Application

### Option A: Local profile with Docker Compose (recommended)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

This will:
- Auto-start a PostgreSQL container via `compose.yml`
- Use mock AI provider (no AWS creds needed)
- Enable debug logging for the application

### Option B: External PostgreSQL

If you already have a PostgreSQL instance running:

```bash
DB_URL=jdbc:postgresql://your-host:5432/phone_scoring \
DB_USER=your_user \
DB_PASSWORD=your_password \
mvn spring-boot:run
```

### Option C: IDE (IntelliJ / VS Code)

1. Open the project in your IDE.
2. Set the active profile to `local` in your run configuration:
   - IntelliJ: Run Configuration → Active Profiles: `local`
   - VS Code (Spring Boot Dashboard): set `spring.profiles.active=local` in launch config
3. Run `PocSpringBatchApplication.main()`.

---

## Testing the APIs

### Using cURL

```bash
# 1. Trigger the batch job (populates the feature store)
curl -X POST http://localhost:8080/api/v1/batch/run

# 2. View today's scored phone numbers (sorted by reach score)
curl http://localhost:8080/api/v1/batch/feature-store

# 3. View scored data for a specific date
curl "http://localhost:8080/api/v1/batch/feature-store?date=2026-03-28"

# 4. View full history for a phone number
curl http://localhost:8080/api/v1/batch/feature-store/history/5551001001

# 5. Get an AI-generated cheat sheet by phone number
curl http://localhost:8080/api/v1/cheat-sheet/by-phone/5551001001

# 6. Get a cheat sheet by user ID
curl http://localhost:8080/api/v1/cheat-sheet/by-user/U001
```

### Using Postman

1. Import the collection: **File → Import → `docs/postman-collection.json`**
2. The collection uses `{{baseUrl}}` variable (default: `http://localhost:8080`).
3. Run requests in order:
   - **"1. Trigger Batch Job"** first to populate data.
   - Then query the feature store and cheat sheets.

### Using Swagger UI

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser to interactively explore and test all endpoints.

---

## API Reference

### Batch Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/batch/run` | Manually trigger the batch job |
| `GET` | `/api/v1/batch/feature-store` | List today's scored entries (reach score desc) |
| `GET` | `/api/v1/batch/feature-store?date=YYYY-MM-DD` | List entries for a specific date |
| `GET` | `/api/v1/batch/feature-store/history/{phoneNumber}` | Full audit history for a phone number |

### Cheat Sheet (AI-generated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/cheat-sheet/by-phone/{phoneNumber}` | Negotiation advice for a phone number |
| `GET` | `/api/v1/cheat-sheet/by-user/{userId}` | Negotiation advice for a user ID |

### Documentation

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/api-docs` | OpenAPI 3.0 JSON spec |

---

## Configuration

### Profiles

| Profile | Database | AI Provider | Docker Compose | Use Case |
|---------|----------|-------------|----------------|----------|
| _(default)_ | PostgreSQL (env vars) | Bedrock | Not managed | Production / staging |
| `local` | PostgreSQL via Docker Compose | Mock | Auto start/stop | Local development |
| `test` | H2 in-memory | Mock | Disabled | Unit/integration tests |

### Key Properties

| Property | Default | Description |
|----------|---------|-------------|
| `app.batch.schedule.cron` | `0 0 5 * * ?` | Daily batch schedule (05:00 AM) |
| `app.dialer.provider` | `five9` | Dialer: `five9` or `aws-connect` |
| `app.ai.provider` | `bedrock` | LLM: `bedrock` or `mock` |
| `app.sources.s3.enabled` | `false` | Enable real S3 reader |
| `app.sources.db.use-replica` | `false` | Enable real DB replica reader |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/phone_scoring` | JDBC URL |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `AWS_REGION` | `us-east-1` | AWS region for Bedrock/S3 |
| `API_SOURCE_URL` | `http://localhost:8081` | External API source URL |

---

## Running Tests

```bash
# Run all tests (uses H2 in-memory database, no Docker needed)
mvn test

# Run a specific test class
mvn test -Dtest=ScoreCalculationServiceTest

# Run tests with verbose output
mvn test -Dsurefire.useFile=false
```

All tests use H2 in-memory and mock beans—**no Docker or PostgreSQL required** for testing.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/poc/springbatch/
│   │   ├── PocSpringBatchApplication.java     # Entry point
│   │   ├── ai/                                # LLM abstraction (Bedrock, Mock)
│   │   ├── batch/
│   │   │   ├── reader/                        # 3 data source readers (API, DB, S3)
│   │   │   ├── processor/                     # Score computation processor
│   │   │   └── writer/                        # Feature store writer
│   │   ├── config/                            # Batch job, scheduler, app config
│   │   ├── controller/                        # REST endpoints
│   │   ├── dialer/                            # Strategy pattern (Five9, AWS Connect)
│   │   ├── entity/                            # JPA entities
│   │   ├── model/                             # DTOs / domain models
│   │   ├── repository/                        # Spring Data JPA repositories
│   │   └── service/                           # Business logic services
│   └── resources/
│       ├── application.yml                    # Default configuration
│       ├── application-local.yml              # Local dev profile (Docker Compose)
│       ├── db/migration/                      # Flyway SQL migrations
│       └── prompts/                           # LLM prompt templates
├── test/
│   ├── java/com/poc/springbatch/              # Unit tests
│   └── resources/application-test.yml         # Test configuration (H2)
├── compose.yml                                # Docker Compose (PostgreSQL)
├── docs/
│   └── postman-collection.json                # Postman API collection
└── pom.xml
```