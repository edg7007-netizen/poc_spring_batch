# Phone Batch — Scoring & Dialer Dispatch POC

A **Spring Boot 3.2 / Spring Batch 5** application that reads phone-number
behavioral features from three heterogeneous sources, computes **reach** and
**recovery** scores, persists results to a lightweight **feature store**, dispatches
a prioritised call list to an external **dialer**, and exposes an AI-powered
**cheat-sheet endpoint** for phone agents.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [Prerequisites](#prerequisites)
4. [Quick Start](#quick-start)
5. [Running Tests](#running-tests)
6. [Testing the Service Locally](#testing-the-service-locally)
7. [Postman Collection](#postman-collection)
8. [H2 Console](#h2-console)
9. [Configuration Reference](#configuration-reference)
10. [Scoring Formulas](#scoring-formulas)
11. [Data Sources (Mock)](#data-sources-mock)
12. [Dialer Strategy Pattern](#dialer-strategy-pattern)
13. [AI Cheat Sheet Service](#ai-cheat-sheet-service)
14. [API Reference](#api-reference)

---

## Architecture Overview

```
┌──────────────┐  ┌───────────────────┐  ┌──────────────┐
│  API Service  │  │  DB Read Replica  │  │    S3 Files   │
│  (mock)       │  │  (mock)           │  │  (mock)       │
└──────┬───────┘  └────────┬──────────┘  └──────┬───────┘
       │                   │                     │
       └───────────┬───────┘─────────────────────┘
                   ▼
       ┌──────────────────────┐
       │ CompositePhoneData   │   merge by phoneNumber
       │ Reader               │
       └──────────┬───────────┘
                  ▼
       ┌──────────────────────┐
       │ PhoneDataProcessor   │   compute reach & recovery scores
       │ (+ ScoringService)   │   using dataset-wide min/max stats
       └──────────┬───────────┘
                  ▼
       ┌──────────────────────┐
       │ FeatureStoreWriter   │   persist → sort by reachScore desc → dialer
       └──────┬──────┬────────┘
              │      │
              ▼      ▼
    ┌─────────┐  ┌──────────────────┐
    │ Feature │  │ DialerStrategy   │
    │ Store   │  │ (Five9 / AWS     │
    │ (H2)    │  │  Connect)        │
    └────┬────┘  └──────────────────┘
         │
         ▼
    ┌──────────────────────────┐
    │ GET /api/v1/cheat-sheet  │   lookup latest entry →
    │      /{phoneNumber}      │   AI advice (Bedrock / Mock)
    └──────────────────────────┘
```

The batch job runs **daily at 05:00 UTC** via `@Scheduled(cron)`.
A manual trigger endpoint is also available for development.

---

## Project Structure

```
src/main/java/com/example/phonebatch/
├── PhoneBatchApplication.java            # Entry point (@EnableScheduling)
├── batch/
│   ├── BatchJobConfig.java               # Job & step Spring Batch beans
│   ├── BatchScheduler.java               # Cron trigger (5 AM daily)
│   ├── processor/
│   │   └── PhoneDataProcessor.java       # ItemProcessor (scoring)
│   ├── reader/
│   │   ├── ApiPhoneDataReader.java       # Mock – loans, app-install
│   │   ├── DatabasePhoneDataReader.java  # Mock – PTP, broken-PTP
│   │   ├── S3PhoneDataReader.java        # Mock – answer rate, recency, balance
│   │   └── CompositePhoneDataReader.java # Merges all readers by phoneNumber
│   └── writer/
│       └── FeatureStoreWriter.java       # Save to DB → sort → dialer
├── controller/
│   ├── CheatSheetController.java         # GET  /api/v1/cheat-sheet/{phone}
│   └── BatchTriggerController.java       # POST /api/v1/batch/run
├── dialer/
│   ├── DialerStrategy.java               # Interface
│   ├── Five9DialerStrategy.java          # Default implementation
│   ├── AwsConnectDialerStrategy.java     # Alternative implementation
│   └── DialerConfig.java                 # Selects strategy from property
├── ai/
│   ├── CheatSheetService.java            # Interface
│   ├── MockCheatSheetService.java        # Default (no AWS needed)
│   └── BedrockCheatSheetService.java     # Stub for AWS Bedrock
├── domain/
│   ├── PhoneNumberRawData.java           # Raw input POJO
│   ├── ScoredPhoneNumber.java            # Scored wrapper
│   └── FeatureStoreEntry.java            # JPA entity (feature_store table)
├── repository/
│   └── FeatureStoreRepository.java       # Spring Data JPA
└── service/
    ├── ScoringService.java               # Score calculation + normalisation
    ├── ScoringStats.java                 # Min/max value object
    ├── ScoringStatsListener.java         # StepExecutionListener → pre-compute stats
    └── DataMergeService.java             # Merge logic for readers

docs/
└── postman-collection.json               # Importable Postman collection
```

---

## Prerequisites

| Tool   | Version |
|--------|---------|
| Java   | 17+     |
| Maven  | 3.8+    |

> No external databases or AWS credentials are required.
> The application uses an in-memory **H2** database and mock data sources by default.

---

## Quick Start

```bash
# 1  Clone the repository
git clone https://github.com/edg7007-netizen/poc_spring_batch.git
cd poc_spring_batch

# 2  Build the project
./mvnw clean package -DskipTests
# or: mvn clean package -DskipTests

# 3  Run the application
java -jar target/poc-spring-batch-0.0.1-SNAPSHOT.jar
# or: ./mvnw spring-boot:run

# The server starts on http://localhost:8080
```

---

## Running Tests

```bash
# Run all tests
./mvnw test
# or: mvn test
```

The test suite includes:

| Test class              | What it validates                                       |
|-------------------------|---------------------------------------------------------|
| `ScoringServiceTest`    | Score calculation, normalisation, edge cases            |
| `DialerStrategyTest`    | Default strategy is Five9, no exceptions                |
| `PhoneDataProcessorTest`| Processor returns valid scores in \[0, 1\] range       |

---

## Testing the Service Locally

### Step-by-step walkthrough

After starting the application (`./mvnw spring-boot:run`), follow these steps:

#### 1. Trigger the batch job manually

The cron schedule runs at 5:00 AM, but you can trigger it instantly:

```bash
curl -X POST http://localhost:8080/api/v1/batch/run
```

**Expected response:**
```json
{
  "status": "COMPLETED",
  "message": "Batch job executed successfully"
}
```

This reads data from the three mock sources, merges by phone number, computes
scores, persists 10 records to the feature store, and dispatches the sorted list
to the Five9 dialer (logged to console).

#### 2. Query the cheat-sheet endpoint

Pick any of the 10 mock phone numbers (`555-0001` through `555-0010`):

```bash
curl http://localhost:8080/api/v1/cheat-sheet/555-0001
```

**Expected response:**
```json
{
  "phoneNumber": "555-0001",
  "advice": "Mock advice for 555-0001: Balance owed: $1200.50. ReachScore: 0.6543. Consider payment plan options."
}
```

#### 3. Try a phone number that doesn't exist

```bash
curl -i http://localhost:8080/api/v1/cheat-sheet/999-0000
```

Returns **HTTP 404 Not Found**.

#### 4. Browse the feature store via H2 Console

Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console) in your
browser and connect with:

| Setting   | Value                                                              |
|-----------|--------------------------------------------------------------------|
| JDBC URL  | `jdbc:h2:mem:phonebatch`                                          |
| User Name | `sa`                                                               |
| Password  | *(leave empty)*                                                    |

Then run:

```sql
SELECT * FROM feature_store ORDER BY reach_score DESC;
```

You'll see all 10 scored phone records ranked by reach score.

---

## Postman Collection

A ready-to-import Postman collection is included at
**[`docs/postman-collection.json`](docs/postman-collection.json)**.

### How to import

1. Open **Postman** → click **Import** (top left)
2. Select the file `docs/postman-collection.json`
3. The collection **Phone Batch POC** appears with 6 requests pre-configured

### Requests included

| #  | Request                               | Method | Path                                  |
|----|---------------------------------------|--------|---------------------------------------|
| 1  | Trigger batch job                     | POST   | `/api/v1/batch/run`                   |
| 2  | Cheat-sheet for 555-0001              | GET    | `/api/v1/cheat-sheet/555-0001`        |
| 3  | Cheat-sheet for 555-0006              | GET    | `/api/v1/cheat-sheet/555-0006`        |
| 4  | Cheat-sheet for 555-0010              | GET    | `/api/v1/cheat-sheet/555-0010`        |
| 5  | Cheat-sheet not found (999-0000)      | GET    | `/api/v1/cheat-sheet/999-0000`        |
| 6  | Open H2 Console                       | GET    | `/h2-console`                         |

> The collection uses a `{{baseUrl}}` variable defaulting to `http://localhost:8080`.
> Change it in *Collection Variables* if your server is on a different port.

---

## H2 Console

The embedded H2 web console is enabled by default and available at
`http://localhost:8080/h2-console`.

**Connection settings:**
- Driver Class: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:phonebatch`
- User Name: `sa`
- Password: *(empty)*

Useful queries:

```sql
-- All scored records, ranked by reachability
SELECT phone_number, reach_score, recovery_score, balance_owed, job_date
FROM   feature_store
ORDER  BY reach_score DESC;

-- Full audit for a specific phone number
SELECT * FROM feature_store WHERE phone_number = '555-0001';
```

---

## Configuration Reference

All properties are in `src/main/resources/application.properties`.

| Property                              | Default       | Description                                                  |
|---------------------------------------|---------------|--------------------------------------------------------------|
| `app.dialer.provider`                 | `five9`       | Dialer backend: `five9` or `aws-connect`                     |
| `app.ai.provider`                     | `mock`        | AI service: `mock` (default) or `bedrock` (requires AWS)     |
| `spring.batch.job.enabled`            | `false`       | Prevents auto-run on startup; the scheduler triggers the job |
| `spring.h2.console.enabled`           | `true`        | Enables the H2 web console at `/h2-console`                  |
| `spring.datasource.url`              | `jdbc:h2:mem:phonebatch` | In-memory database URL                              |

### Switching to AWS Connect dialer

```properties
app.dialer.provider=aws-connect
```

### Enabling Bedrock AI (requires AWS credentials)

```properties
app.ai.provider=bedrock
```

---

## Scoring Formulas

Scores are normalised to the **\[0, 1\]** range using min-max scaling across the
full dataset (computed once per job by `ScoringStatsListener` before the step runs).

### Reach Score

Measures how likely a phone number is to be answered.

```
reachScore = historicalAnswerRate × 0.40
           + appInstalled         × 0.20       (1.0 if true, 0.0 if false)
           + 1/(1 + daysSinceLastContact) × 0.20
           + norm(previousLoans)  × 0.10
           + (1 − norm(promiseToPay)) × 0.10   (more PTPs = harder to reach)
```

### Recovery Score

Measures the expected recovery value of contacting this number.

```
recoveryScore = norm(balanceOwed)        × 0.30
              + (brokenPtp ? 0 : 1)      × 0.25  (healthy PTP = higher score)
              + norm(promiseToPay)        × 0.20
              + historicalAnswerRate      × 0.15
              + norm(previousLoans)       × 0.10
```

The phone list sent to the dialer is sorted **descending by reach score**.

---

## Data Sources (Mock)

All three readers return data for the same 10 phone numbers (`555-0001` to `555-0010`).

| Source               | Simulates          | Fields provided                                         |
|----------------------|--------------------|---------------------------------------------------------|
| `ApiPhoneDataReader` | REST API call      | `previousLoans`, `appInstalled`                         |
| `DatabasePhoneDataReader` | DB read replica | `promiseToPay`, `brokenPtp`                            |
| `S3PhoneDataReader`  | S3 file download   | `historicalAnswerRate`, `daysSinceLastContact`, `balanceOwed` |

The `CompositePhoneDataReader` merges them by `phoneNumber` into a single
`PhoneNumberRawData` object per phone.

<details>
<summary><strong>Mock data table</strong> (click to expand)</summary>

| Phone    | Loans | PTP | Answer Rate | Days Since Contact | Broken PTP | App Installed | Balance    |
|----------|-------|-----|-------------|--------------------|------------|---------------|------------|
| 555-0001 | 3     | 2   | 0.72        | 14                 | no         | yes           | $1,200.50  |
| 555-0002 | 1     | 0   | 0.45        | 30                 | no         | no            | $3,400.00  |
| 555-0003 | 5     | 3   | 0.88        | 5                  | yes        | yes           | $800.75    |
| 555-0004 | 2     | 1   | 0.60        | 21                 | no         | yes           | $5,500.00  |
| 555-0005 | 7     | 4   | 0.33        | 60                 | yes        | no            | $9,800.00  |
| 555-0006 | 0     | 0   | 0.91        | 2                  | yes        | yes           | $250.00    |
| 555-0007 | 4     | 2   | 0.55        | 45                 | yes        | no            | $4,200.00  |
| 555-0008 | 6     | 5   | 0.78        | 10                 | no         | yes           | $1,750.25  |
| 555-0009 | 2     | 1   | 0.40        | 35                 | no         | no            | $6,700.00  |
| 555-0010 | 8     | 3   | 0.25        | 90                 | yes        | no            | $12,000.00 |

</details>

---

## Dialer Strategy Pattern

The dialer is abstracted behind the `DialerStrategy` interface so swapping
providers requires **zero code changes** — only the `app.dialer.provider` property:

```
DialerStrategy (interface)
├── Five9DialerStrategy       ← default ("five9")
└── AwsConnectDialerStrategy  ← alternative ("aws-connect")
```

Both implementations currently log to console. Replace the `sendPhoneList()`
body with real API calls when ready.

---

## AI Cheat Sheet Service

The cheat-sheet endpoint generates strategic negotiation advice by:

1. Looking up the latest `FeatureStoreEntry` for the given phone number.
2. Passing the entry to a `CheatSheetService` implementation.
3. Returning the advice as JSON.

```
CheatSheetService (interface)
├── MockCheatSheetService        ← default (no AWS needed)
└── BedrockCheatSheetService     ← activates with app.ai.provider=bedrock
```

In production the Bedrock service would fill a prompt template with the client's
features and call Amazon Bedrock via Spring AI for strategic recovery advice.

---

## API Reference

### `POST /api/v1/batch/run`

Manually triggers the phone scoring batch job.

**Response (200):**
```json
{
  "status": "COMPLETED",
  "message": "Batch job executed successfully"
}
```

**Response (500):**
```json
{
  "status": "FAILED",
  "message": "<error detail>"
}
```

---

### `GET /api/v1/cheat-sheet/{phoneNumber}`

Returns AI-generated negotiation advice for a phone number.

| Parameter     | Type   | Location | Required | Example    |
|---------------|--------|----------|----------|------------|
| `phoneNumber` | string | path     | yes      | `555-0001` |

**Response (200):**
```json
{
  "phoneNumber": "555-0001",
  "advice": "Mock advice for 555-0001: Balance owed: $1200.50. ReachScore: 0.6543. Consider payment plan options."
}
```

**Response (404):** empty body — phone number has no entries in the feature store.