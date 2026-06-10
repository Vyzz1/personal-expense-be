# Transactional Outbox Pattern

## Problem Statement

In an event-driven system, after a business operation completes (e.g. creating a transaction), other modules need to be notified (e.g. budget must update its `spentAmount`).

The naive approach is to publish an event after committing to the database:

```
1. BEGIN TX → save Transaction → COMMIT
2. applicationEventPublisher.publishEvent(event)   ← what if the app crashes here?
```

If the app crashes after step 1 but before step 2, the transaction is saved but the budget **is never updated**. This is the **dual-write problem** — two systems (database and event bus) cannot share a single transaction.

The **Transactional Outbox** pattern solves this by treating the event as a database row, written **in the same transaction** as the business operation. A separate worker then reads and publishes it.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Business Transaction                  │
│                                                          │
│   BEGIN TX                                               │
│     INSERT INTO transactions (...)       ← business data │
│     INSERT INTO outbox_messages (...)    ← domain event  │
│   COMMIT                                                 │
└─────────────────────────────────────────────────────────┘
                          │
                          │  (DB commit succeeds)
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    OutboxWorker                          │
│   @Scheduled(fixedDelay = 15_000ms)                      │
│                                                          │
│   SELECT * FROM outbox_messages                          │
│     WHERE processed_at IS NULL                           │
│                                                          │
│   for each message:                                      │
│     Class<?> clazz = Class.forName(eventType)            │
│     Object event = objectMapper.readValue(payload, clazz)│
│     applicationEventPublisher.publishEvent(event)        │
│     message.processedAt = Instant.now()                  │
│     save(message)                                        │
└─────────────────────────────────────────────────────────┘
                          │
          ┌───────────────┼────────────────┐
          ▼               ▼                ▼
   BudgetTransaction   MonthlyExpense   (future listeners)
   HandlerService      Updater
```

---

## Components

### 1. `DomainEvent` — marker interface

```java
// shared/domain/DomainEvent.java
public interface DomainEvent {}
```

All events in the system implement this interface so `OutboxPersistenceAdapter` can accept any event type generically.

### 2. Domain Events

| Event | Module | Emitted when |
|---|---|---|
| `TransactionCreatedEvent` | transaction | A single transaction is created |
| `TransactionUpdatedEvent` | transaction | A transaction is updated |
| `TransactionDeletedEvent` | transaction | A transaction is deleted |
| `TransactionChunkCreatedEvent` | transaction | After each chunk in a batch import (wraps multiple `TransactionCreatedEvent`s) |

### 3. `OutboxRepositoryPort` — output port

```java
public interface OutboxRepositoryPort {
    void saveOutboxMessage(DomainEvent event, String module);
}
```

Injected into any adapter that needs to write an event to the outbox.

### 4. `OutboxPersistenceAdapter` — infrastructure adapter

```java
@Service
public class OutboxPersistenceAdapter implements OutboxRepositoryPort {

    @Override
    public void saveOutboxMessage(DomainEvent event, String module) {
        OutboxMessageJpaEntity outboxMessage = new OutboxMessageJpaEntity();
        outboxMessage.setModule(module);
        outboxMessage.setPayload(objectMapper.writeValueAsString(event));  // JSON
        outboxMessage.setEventType(event.getClass().getName());            // fully-qualified class name
        entityManager.persist(outboxMessage);
    }
}
```

Uses `EntityManager` directly (not a Spring Data repository) to ensure the `persist` call participates in the **caller's active transaction**, rather than creating a new one.

### 5. `outbox_messages` — database table

```sql
CREATE TABLE outbox_messages (
    id           UUID         PRIMARY KEY,
    module       VARCHAR(100) NOT NULL,    -- source module (e.g. "Transaction")
    event_type   VARCHAR(255) NOT NULL,    -- fully-qualified class name of the event
    payload      TEXT         NOT NULL,    -- JSON-serialized event body
    created_at   TIMESTAMP    NOT NULL,    -- auto-set by @CreatedDate
    processed_at TIMESTAMP,               -- NULL = not yet delivered
    error        VARCHAR(1000)            -- error message if delivery failed
);
```

### 6. `OutboxWorker` — scheduled poller

```java
@Scheduled(fixedDelay = 15_000)    // runs 15s after the previous execution completes
@Transactional
public void process() {
    List<OutboxMessageJpaEntity> messages =
        outboxPersistenceJpaRepository.findByProcessedAtIsNull();

    for (OutboxMessageJpaEntity message : messages) {
        try {
            Class<?> eventClass = Class.forName(message.getEventType());
            Object event = objectMapper.readValue(message.getPayload(), eventClass);
            eventPublisher.publishEvent(event);
            message.setProcessedAt(Instant.now());
        } catch (Exception e) {
            message.setError(e.getMessage());  // record the error, do NOT rethrow → continue to next message
        } finally {
            outboxPersistenceJpaRepository.save(message);
        }
    }
}
```

**Design notes:**
- `fixedDelay` (not `fixedRate`) — the next run only starts after the previous one **fully completes**, preventing concurrent access to the same rows
- `@Transactional` on `process()` — all `save()` calls within the loop share one transaction
- An error on one message does not stop the batch — remaining messages are still processed

---

## Example Flow: Create Transaction → Update Budget

```
POST /api/v1/transactions
                │
                ▼
   TransactionCommandService.createTransaction()
                │
                ▼
   TransactionPersistenceAdapter          [BEGIN TX]
     ├─ transactionRepository.save(transaction)
     └─ outboxRepositoryPort.saveOutboxMessage(
            TransactionCreatedEvent { transactionId, amount, categoryId, userId, type },
            "Transaction"
        )
   [COMMIT TX]  ← transaction row + outbox row committed atomically
                │
                │  ... up to 15 seconds later ...
                ▼
   OutboxWorker.process()
     ├─ Reads outbox_messages WHERE processed_at IS NULL
     ├─ Deserializes → TransactionCreatedEvent
     ├─ applicationEventPublisher.publishEvent(event)
     └─ Sets processedAt = now()
                │
                ▼
   BudgetTransactionHandlerService.handle(TransactionCreatedEvent)
     ├─ Finds the budget matching user + category + current period
     ├─ Adds amount to budget.spentAmount
     ├─ Recalculates budget.status (ACTIVE / EXCEEDED)
     └─ Saves the updated budget
```

---

## Guarantees and Trade-offs

### Guarantees

| Property | Description |
|---|---|
| **At-least-once delivery** | If the worker crashes after publishing but before setting `processedAt`, the message will be re-read and re-published on the next poll cycle |
| **No message loss** | Events are always persisted in the same transaction as the business operation — an event cannot be lost if the business operation succeeded |
| **Eventual consistency** | Downstream state (budget, monthly expense) is updated within ~15 seconds of the originating operation |

### Trade-offs

| Limitation | Reason |
|---|---|
| **Not real-time** | Maximum ~15-second lag between a business operation and its side effects |
| **At-least-once (not exactly-once)** | Listeners should be **idempotent** if an event may be delivered more than once (no idempotency key is currently enforced) |
| **Polling overhead** | Queries `WHERE processed_at IS NULL` every 15s — an index on that column is advisable at scale |

---

## Code Locations

```
shared/
├── domain/
│   └── DomainEvent.java                           ← marker interface
├── application/port/out/
│   └── OutboxRepositoryPort.java                  ← output port
└── infrastructure/
    ├── persistence/
    │   ├── OutboxMessageJpaEntity.java             ← JPA entity
    │   ├── OutboxPersistenceAdapter.java           ← implements OutboxRepositoryPort
    │   └── OutboxPersistenceJpaRepository.java     ← Spring Data repository
    └── worker/
        └── OutboxWorker.java                       ← @Scheduled poller

modules/transaction/domain/event/
├── TransactionCreatedEvent.java
├── TransactionUpdatedEvent.java
├── TransactionDeletedEvent.java
└── TransactionChunkCreatedEvent.java

modules/budget/application/listener/
└── BudgetTransactionHandlerService.java            ← event consumer
```
