# Batch Processing — CSV Transaction Import

## Overview

The batch import feature allows users to upload a CSV file containing multiple transactions at once. Instead of inserting each record through the standard REST API, Spring Batch splits the file into fixed-size chunks, processes them in sequence, and batch-inserts into the database — enabling thousands of rows to be imported without blocking the request thread.

The job runs **asynchronously**. The API returns a `batchId` immediately; the client polls that ID to track progress.

---

## Processing Pipeline

```
POST /api/v1/transactions/batch
       │  (multipart/form-data, field: file)
       ▼
TransactionBatchController
  ├─ Validate original filename
  ├─ Sanitize filename (strip path traversal, replace special chars)
  ├─ Copy to system temp dir  (UUID prefix to avoid collisions)
  └─ importTransactionUseCase.importTransactions(userId, filePath)
            │
            ▼
    TransactionBatchAdapter.executeBatchImport()
      ├─ Build JobParameters { userId, filePath, time }
      └─ jobOperator.start(importJob, params)  ← async, returns immediately
                │
                ▼
         Spring Batch Job: "import-job"
          │
          ├─ Step 1: "validation-step"
          │    │
          │    ├─ FlatFileItemReader<TransactionCsv>
          │    │     - Skips header (line 1)
          │    │     - Delimiter: ","
          │    │     - Columns: amount, description, date, type, category
          │    │
          │    ├─ TransactionValidationProcessor
          │    │     - Validates amount > 0
          │    │     - Validates description, date, type, category are not empty
          │    │     - Parses date (format: M/d/yyyy)
          │    │     - Resolves category name → UUID (auto-creates if missing for userId)
          │    │     - Builds TransactionJpaEntity
          │    │
          │    ├─ JpaItemWriter<TransactionJpaEntity>
          │    │     - Chunk size: 10
          │    │     - usePersist=true (INSERT, not merge)
          │    │
          │    ├─ TransactionChunkListener    (logs before/after each chunk)
          │    │
          │    ├─ TransactionItemWriterListener.afterWrite()
          │    │     - Runs INSIDE the chunk transaction (same TX as JpaItemWriter)
          │    │     - Wraps chunk items into one TransactionChunkCreatedEvent
          │    │     - Writes to outbox_messages  ← ATOMIC with the transaction inserts
          │    │
          │    └─ Fault-tolerant config:
          │          - skip(IllegalArgumentException)          → skip invalid records
          │          - skip(DataIntegrityViolationException)   → skip DB constraint violations
          │          - skipLimit: 10  (>10 skipped records → step FAILED)
          │
          └─ Step 2: "summary-step"
               └─ JobSummaryTasklet
                     - Aggregates: read / written / skipped / filtered counts
                     - Calculates total duration (ms)
                     - Sends SSE notification to user via NotifyBatchCompletionUseCase
```

---

## CSV Format

The first row must be the header (it is skipped during reading):

```
amount,description,date,type,category
```

Example data:

```csv
amount,description,date,type,category
150000,Lunch,6/10/2026,EXPENSE,Food
5000000,June salary,6/1/2026,INCOME,Income
80000,Grab ride home,6/9/2026,EXPENSE,Transportation
```

| Column | Type | Required | Notes |
|---|---|---|---|
| `amount` | Decimal | Yes | Must be > 0 |
| `description` | String | Yes | Cannot be blank |
| `date` | String | Yes | Format `M/d/yyyy` (e.g. `6/10/2026`) |
| `type` | String | Yes | `INCOME` or `EXPENSE` |
| `category` | String | Yes | Category name; auto-created if not found for this userId |

---

## API Endpoints

### Upload CSV

```
POST /api/v1/transactions/batch
Content-Type: multipart/form-data
Authorization: Bearer <token>

file=<binary CSV content>
```

Response (`202 Accepted`):

```json
{
  "success": true,
  "message": "Batch import started",
  "data": {
    "batchId": "42",
    "status": "STARTING"
  }
}
```

### Poll Job Status

```
GET /api/v1/transactions/batch/{batchId}
Authorization: Bearer <token>
```

Response:

```json
{
  "success": true,
  "message": "Batch import status retrieved",
  "data": {
    "batchId": "42",
    "status": "COMPLETED"
  }
}
```

**Spring Batch statuses:**

| Status | Meaning |
|---|---|
| `STARTING` | Job has been submitted |
| `STARTED` | Job is running |
| `COMPLETED` | Finished successfully (some records may have been skipped) |
| `FAILED` | Job failed (exceeded `skipLimit` or a system error occurred) |
| `STOPPED` | Job was manually stopped |

---

## Error Handling and Fault Tolerance

### Skip logic

The processor throws `IllegalArgumentException` on invalid records. The step is configured with `faultTolerant().skip(IllegalArgumentException.class).skipLimit(10)`:

- Invalid records are skipped and not written to the database
- The rest of the records in the same chunk are still committed
- If **more than 10 records** are skipped → `JobStatus = FAILED`

### Atomicity — Outbox inside chunk transaction

`TransactionItemWriterListener.afterWrite()` runs inside the `JpaItemWriter`'s transaction, ensuring:

```
BEGIN TRANSACTION
  ├─ JpaItemWriter writes 10 transactions
  └─ afterWrite() writes 1 outbox_messages row
COMMIT  ← both committed, or both rolled back on error
```

There is no scenario where transactions are persisted but the outbox message is lost.

### SSE Completion Notification

After the job completes, `JobSummaryTasklet` pushes an SSE notification to any client subscribed under the matching `userId` via `GET /api/v1/notifications/subscribe`. The payload contains the job summary:

```json
{
  "totalRead": 100,
  "totalWritten": 98,
  "totalSkipped": 2
}
```

Client subscribes to:
```
GET /api/v1/notifications/subscribe
Accept: text/event-stream
Authorization: Bearer <token>
```

A heartbeat event is sent every 30 seconds to keep the connection alive. The connection timeout is 3 minutes.

---

## Class Reference

| Class | Package | Responsibility |
|---|---|---|
| `TransactionBatchController` | `presentation` | Receives file upload, sanitizes path, calls use case |
| `TransactionBatchAdapter` | `infrastructure/batch` | Bridges `TransactionBatchPort` to Spring Batch `JobOperator` |
| `TransactionBatchJobConfig` | `infrastructure/batch` | Declares `Job`, `Step`, `Reader`, `Writer` beans |
| `TransactionValidationProcessor` | `infrastructure/batch` | Validates and transforms `TransactionCsv` → `TransactionJpaEntity` |
| `TransactionChunkListener` | `infrastructure/batch` | Logs chunk lifecycle events |
| `TransactionItemWriterListener` | `infrastructure/batch` | Writes outbox message after each chunk (atomic with DB write) |
| `JobSummaryTasklet` | `infrastructure/batch` | Aggregates job results and sends SSE notification |
| `BathAsyncConfiguration` | `infrastructure/batch` | Configures `@Async` executor for `JobOperator` |
| `TransactionCsv` | `application/dto` | Record DTO for one CSV row |
