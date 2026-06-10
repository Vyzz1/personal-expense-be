# Improvement Backlog

Findings from a full codebase audit. Grouped by severity — fix HIGH items before touching anything else.

---

## HIGH

### H1 — SSE memory leak in `SseNotificationService`
**File:** `src/main/java/com/huynh/personal_expense_be/shared/notification/service/SseNotificationService.java:32–42`

Every subscriber spawns a scheduled heartbeat task, but the disconnect callbacks all pass `null` for the future reference:

```java
emitter.onCompletion(() -> removeEmitter(userId, emitter, null));
emitter.onTimeout(()    -> removeEmitter(userId, emitter, null));
emitter.onError(e       -> removeEmitter(userId, emitter, null));
```

`removeEmitter()` only cancels the future when it is non-null, so the heartbeat task keeps firing indefinitely after disconnect. Zombie tasks accumulate over time and will eventually exhaust the `ScheduledExecutorService` thread pool.

**Fix:** Store the `ScheduledFuture` on the emitter (e.g. in a companion map keyed by emitter identity) so all removal paths can cancel it.

---

### H2 — Authorization bypass on Budget and Category delete
**Files:**
- `src/main/java/com/huynh/personal_expense_be/modules/budget/presentation/BudgetController.java:91`
- `src/main/java/com/huynh/personal_expense_be/modules/budget/application/service/CommandBudgetService.java:74`
- `src/main/java/com/huynh/personal_expense_be/modules/category/presentation/CategoryController.java:93`

The delete endpoints do not pass `Principal` to the service and the service does not verify ownership:

```java
// BudgetController.java:91 — no Principal, no userId
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable UUID id) {
    deleteBudgetUseCase.deleteBudget(id);
}

// CommandBudgetService.java:74 — deletes by id only
public void deleteBudget(UUID budgetId) {
    budgetRepositoryPort.deleteById(budgetId);
}
```

Any authenticated user can delete any other user's budget or category by knowing the UUID.

**Fix:** Pass `userId` from `Principal` down through the use case and add a `WHERE id = ? AND user_id = ?` guard in the repository.

---

### H3 — Race condition in batch category auto-creation
**File:** `src/main/java/com/huynh/personal_expense_be/modules/transaction/infrastructure/batch/TransactionValidationProcessor.java:51`

```java
Category category = categoryRepository.existsByNameAndUserId(item.category(), item.userId()).orElse(null);
if (category == null) {
    category = categoryRepository.save(Category.builder()
            .name(item.category()).userId(item.userId()).build());
}
```

Two concurrent batch jobs processing transactions with the same new category name will both see `null` and attempt to insert, causing either a duplicate row or a constraint violation that skips the record.

**Fix:** Add a unique constraint on `(user_id, name)` in the `categories` table and handle `DataIntegrityViolationException` in the processor by re-fetching the existing row.

---

### H4 — OutboxWorker silently drops failed events
**File:** `src/main/java/com/huynh/personal_expense_be/shared/infrastructure/worker/OutboxWorker.java:37–55`

When `publishEvent()` throws, the worker writes the error string to the `error` column and moves on. The message is never retried — the event is permanently lost:

```java
} catch (Exception e) {
    message.setError(e.getMessage());  // marked as errored, never retried
} finally {
    outboxPersistenceJpaRepository.save(message);
}
```

Additionally, there is no idempotency guard: if the worker is restarted between publishing and setting `processedAt`, the same event is published twice.

**Fix:**
- Add a `retry_count` column; retry up to N times before giving up.
- Add a `locked_until` / `in_flight` flag so a crashed worker does not cause double-delivery.
- Consider a dead-letter table for messages that exceed the retry limit.

---

### H5 — NPE in `Budget.subtractSpentAmount()`
**File:** `src/main/java/com/huynh/personal_expense_be/modules/budget/domain/Budget.java`

`addSpentAmount()` guards against a null `spentAmount`, but `subtractSpentAmount()` does not:

```java
// addSpentAmount — null-safe
BigDecimal current = this.spentAmount != null ? this.spentAmount : BigDecimal.ZERO;

// subtractSpentAmount — will throw NPE if spentAmount is null
return this.toBuilder()
        .spentAmount(this.spentAmount.subtract(amount))
        .build();
```

**Fix:** Apply the same null-coalescing pattern as `addSpentAmount()`.

---

## MEDIUM

### M1 — No upper bound on pagination `size`
**File:** `src/main/java/com/huynh/personal_expense_be/modules/transaction/infrastructure/persistence/TransactionPersistenceAdapter.java:157`

```java
int size = command.size() > 0 ? command.size() : 10;  // no maximum
dataQuery.setMaxResults(size);
```

A client can send `?size=999999` and fetch the entire table in one query, causing OOM and slow DB response. The same issue likely exists in the budget and category query adapters.

**Fix:** Cap `size` at a reasonable maximum (e.g. 100) in the command or at the query layer.

---

### M2 — `sortBy` field is not whitelisted in transaction queries
**File:** `src/main/java/com/huynh/personal_expense_be/modules/transaction/infrastructure/persistence/TransactionPersistenceAdapter.java:92`

```java
String sortBy = command.sortBy() != null ? command.sortBy() : "occurredAt";
// ...
"... ORDER BY t." + sortBy  // direct string concatenation into JPQL
```

The budget repository correctly whitelists allowed sort fields; the transaction repository does not, allowing arbitrary field names to be injected into the query string.

**Fix:** Whitelist the allowed `sortBy` values (same approach used in the budget repository) and reject or default on unknown values.

---

### M3 — Test coverage is critically low
**Directory:** `src/test/`

Approximately 25 test files exist for 150+ source files. The following critical paths have zero test coverage:

- `OutboxWorker` — async event delivery, retry, error handling
- `TransactionValidationProcessor` — CSV parsing, validation rules, category resolution
- `SseNotificationService` — subscription lifecycle, heartbeat, cleanup
- All controller authorization checks (no test verifies the userId ownership rules)
- Batch import end-to-end flow

**Fix:** Target ≥ 70% line coverage. Start with unit tests for the outbox worker and batch processor, then add integration tests for the authorization scenarios.

---

### M4 — CORS allows all origins
**File:** `src/main/java/com/huynh/personal_expense_be/shared/config/CorsConfig.java`

Default `allowedOriginPatterns = ["*"]` is overly permissive for a financial application. Even with `allowCredentials = false`, it is best practice to restrict origins to known frontend URLs.

**Fix:** Set `allowedOrigins` to the actual frontend origin(s) via an environment variable, not a wildcard.

---

### M5 — Threshold percentage normalization has unreachable branch
**File:** `src/main/java/com/huynh/personal_expense_be/modules/budget/domain/Budget.java:62`

```java
BigDecimal normalizedThreshold = newThresholdPercentage > 1
        ? BigDecimal.valueOf(newThresholdPercentage).divide(BigDecimal.valueOf(100))
        : BigDecimal.valueOf(newThresholdPercentage);  // ← never reached
```

`CreateBudgetRequest` enforces the value is between 50.0 and 100.0, so it is always > 1. The `≤ 1` branch is dead code that creates a false impression of dual-format support.

**Fix:** Remove the conditional and always divide by 100, or document clearly that the accepted range is 0–100 (not 0–1).

---

## LOW

### L1 — No validation on `month` and `year` query parameters
**File:** `src/main/java/com/huynh/personal_expense_be/modules/transaction/presentation/request/GetTransactionRequest.java`

`month` and `year` fields have no `@Min`/`@Max` constraints. A value like `month=13` or `year=-1` reaches the query layer unchecked.

**Fix:** Add `@Min(1) @Max(12)` on `month` and a reasonable range constraint on `year`.

---

### L2 — Hardcoded UUID in `BudgetTransactionHandlerService`
**File:** `src/main/java/com/huynh/personal_expense_be/modules/budget/application/service/BudgetTransactionHandlerService.java:32`

```java
private static final UUID UNCATEGORIZED_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
```

**Fix:** Externalize to `application.yaml` as `app.budget.uncategorized-id` and inject via `@Value`.

---

### L3 — No rate limiting on SSE subscription endpoint
**File:** `src/main/java/com/huynh/personal_expense_be/shared/notification/presentation/SseNotificationController.java`

`GET /api/v1/notifications/subscribe` has no protection against rapid reconnects or subscription spam from a single user. Each call creates a new `SseEmitter` and a new heartbeat task.

**Fix:** Limit the number of concurrent emitters per `userId` (e.g. evict the oldest when a new one is registered beyond a cap of 3).

---

### L4 — Inconsistent logging style
Several files mix `@Slf4j` (Lombok-generated `log` field) with manually declared `Logger logger` fields. Example: `TransactionChunkListener.java` and `TransactionItemWriterListener.java` both declare `Logger log = LoggerFactory.getLogger(...)` instead of using `@Slf4j`.

**Fix:** Standardize on `@Slf4j` across all classes.

---

### L5 — Actuator endpoints fully exposed in dev mode
**File:** `src/main/java/com/huynh/personal_expense_be/shared/config/SecurityConfig.java:33`

```java
if (isDevMode) {
    auth.requestMatchers("/actuator/**").permitAll();
}
```

This exposes heap dumps, environment variables, and thread state to any unauthenticated request in local development. If a developer accidentally runs with `SPRING_PROFILES_ACTIVE=local` pointing at a shared DB, this becomes a real exposure.

**Fix:** At minimum restrict to `localhost` via Nginx or Spring's `management.server.port`, or require authentication even in dev.

---

## Priority Order

| # | Item | Effort | Impact |
|---|---|---|---|
| 1 | H2 — Budget/Category delete auth bypass | Low | Critical |
| 2 | H1 — SSE memory leak | Medium | High |
| 3 | H4 — OutboxWorker retry + idempotency | Medium | High |
| 4 | H3 — Batch category race condition | Low | Medium |
| 5 | H5 — NPE in subtractSpentAmount | Low | Medium |
| 6 | M1 — Pagination size cap | Low | Medium |
| 7 | M2 — sortBy whitelist in transaction query | Low | Medium |
| 8 | M3 — Test coverage | High | High |
| 9 | M4 — CORS restrict to known origins | Low | Low |
| 10 | L1 — month/year validation | Low | Low |
| 11 | L3 — SSE subscription rate limit | Medium | Low |
| 12 | L2 / L4 / L5 — Config, logging, actuator | Low | Low |
