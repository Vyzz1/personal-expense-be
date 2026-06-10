# Personal Expense Backend

A RESTful backend service for personal expense tracking, built with **Spring Boot 4.0.2** and **Java 21**. Follows a **modular monolith** architecture with hexagonal (ports & adapters) design per module, event-driven consistency via the Transactional Outbox pattern, and bulk import via Spring Batch.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
  - [Module Structure](#module-structure)
  - [Layer Organization](#layer-organization)
  - [Event-Driven Flow](#event-driven-flow)
  - [Batch Processing](#batch-processing)
- [Domain Modules](#domain-modules)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Security & Authentication](#security--authentication)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Run with Docker Compose](#run-with-docker-compose)
  - [Run Locally](#run-locally)
- [Environment Variables](#environment-variables)
- [Project Structure](#project-structure)

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| Build | Maven |
| Database | PostgreSQL 18 |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Auth | Keycloak (OAuth2 + JWT) |
| Batch | Spring Batch |
| Reverse Proxy | Nginx |
| Code Quality | SonarQube, JaCoCo, Checkstyle |
| Containerization | Docker, Docker Compose |

---

## Architecture

### Module Structure

The project is a **modular monolith** — a single deployable unit with clearly bounded modules that communicate through well-defined interfaces, not direct coupling.

```
src/main/java/com/huynh/personal_expense_be/
├── modules/
│   ├── transaction/     # Core transaction recording & CSV batch import
│   ├── budget/          # Budget management & threshold tracking
│   ├── category/        # Hierarchical expense categories
│   └── expense/         # Monthly aggregated expense summaries
└── shared/              # Cross-cutting infrastructure
    ├── config/          # Security, CORS, async configuration
    ├── exception/       # Global exception handler & custom exceptions
    ├── response/        # BaseResponse, PaginationResponse wrappers
    ├── notification/    # Server-Sent Events (SSE) notifications
    ├── domain/          # DomainEvent marker interface
    └── infrastructure/
        ├── persistence/ # Outbox message repository & adapter
        └── worker/      # OutboxWorker scheduled task
```

### Layer Organization

Each module follows **Hexagonal Architecture** with strict layer separation:

```
module/
├── presentation/          # REST controllers + request DTOs (inbound adapter)
├── application/
│   ├── port/
│   │   ├── in/           # Use case interfaces (e.g., CreateTransactionUseCase)
│   │   └── out/          # Output port interfaces (e.g., TransactionRepositoryPort)
│   ├── service/          # Command & Query service implementations
│   ├── listener/         # Domain event listeners
│   └── dto/              # Commands, responses, page results
├── domain/               # Entities, value objects, domain events, business logic
└── infrastructure/
    ├── persistence/      # JPA entities, adapters, mappers (outbound adapter)
    ├── batch/            # Spring Batch job configuration
    └── worker/           # Scheduled tasks
```

**Key patterns:**
- **Command/Query Separation** — write operations use `*CommandService`, reads use `*QueryService`
- **Use Case interfaces** — controllers depend on ports, not implementations
- **Adapter Pattern** — `*PersistenceAdapter` implements output ports, decoupling domain from JPA

### Event-Driven Flow

The system uses the **Transactional Outbox Pattern** for reliable event delivery without a message broker.

```
1. Business operation executes
   └─ e.g., transaction created / deleted / updated

2. Domain event + business data saved atomically
   └─ outbox_messages row inserted in the same DB transaction
   └─ Prevents message loss even if the app crashes after the write

3. OutboxWorker (every 15 seconds)
   └─ Polls unprocessed rows from outbox_messages
   └─ Deserializes payload and publishes to Spring ApplicationEventPublisher

4. Event listeners handle the event
   └─ BudgetTransactionHandlerService recalculates budget.spentAmount and status
   └─ May trigger SSE push notifications to connected clients

5. Message marked as processed
   └─ processedAt timestamp set; errors recorded in error column
```

**Example: Create Transaction → Update Budget**

```
POST /api/v1/transactions
  → TransactionController
  → CreateTransactionUseCase (port)
  → TransactionCommandService
  → TransactionPersistenceAdapter
      ├─ saves Transaction to DB
      └─ saves TransactionCreatedEvent to outbox_messages
           ↓ (15s later via OutboxWorker)
  → BudgetTransactionHandlerService
      └─ updates budget spentAmount + recalculates status
```

### Batch Processing

CSV bulk imports are handled by a Spring Batch pipeline:

```
CSV File Upload
  → FlatFileItemReader    (parses: amount, description, date, type, category)
  → TransactionValidationProcessor  (validates data, resolves category names → UUIDs)
  → JpaItemWriter         (chunk-size: 10, batch inserts to DB)
  → TransactionChunkListener / TransactionItemWriterListener  (event hooks)
  → JobSummaryTasklet     (generates import result report)
```

The job runs asynchronously (`@Async`), returning a batch job ID immediately. Clients can poll `GET /api/v1/transactions/batch/{id}` for status.

---

## Domain Modules

### Transaction
The core domain. Records individual income/expense events and supports CSV bulk imports.

- **Entities:** `Transaction` (id, userId, categoryId, amount, type, description, occurredAt)
- **Events:** `TransactionCreatedEvent`, `TransactionUpdatedEvent`, `TransactionDeletedEvent`
- **Batch:** CSV import with validation, async processing, per-item error capture

### Budget
Tracks spending against a defined limit for a category within a time period.

- **Entities:** `Budget` (id, userId, categoryId, name, limitAmount, spentAmount, status, period, thresholdPercentage)
- **Statuses:** `ACTIVE`, `EXCEEDED`, `EXPIRED`
- **Behavior:** Automatically recalculates `spentAmount` and `status` when transactions change via domain events

### Category
Hierarchical grouping for transactions and budgets.

- **Entities:** `Category` (id, userId, name, parentId)
- **Features:** Parent-child relationships, per-user categories, spending analysis by month/year

### Expense
Aggregated monthly summaries of user spending.

- **Entities:** `MonthlyExpense` (id, userId, month, year, totalAmount)
- **Unique constraint:** one record per `(userId, month, year)`

---

## API Reference

All endpoints are prefixed with `/api/v1` and require a valid **Bearer JWT** token.

### Transactions

| Method | Path | Description |
|---|---|---|
| `POST` | `/transactions` | Create a transaction |
| `GET` | `/transactions` | List transactions (paginated, filterable) |
| `GET` | `/transactions/{id}` | Get transaction detail |
| `PUT` | `/transactions/{id}` | Update a transaction |
| `DELETE` | `/transactions/{id}` | Soft-delete a transaction |
| `POST` | `/transactions/batch` | Bulk import from CSV (`multipart/form-data`, field: `file`) |
| `GET` | `/transactions/batch/{id}` | Get batch import job status |

**GET /transactions query parameters:**

| Parameter | Type | Description |
|---|---|---|
| `page` | int | Page number (0-indexed) |
| `size` | int | Page size |
| `sortBy` | string | Field to sort by |
| `sortOrder` | string | `ASC` or `DESC` |
| `description` | string | Filter by description (partial match) |
| `categoryIds` | UUID[] | Filter by category IDs |
| `type` | string | Filter by type (`INCOME` / `EXPENSE`) |
| `fromDate` | datetime | Start of date range |
| `toDate` | datetime | End of date range |
| `month` | int | Filter by month |
| `year` | int | Filter by year |
| `minAmount` | decimal | Minimum amount filter |
| `maxAmount` | decimal | Maximum amount filter |

### Budgets

| Method | Path | Description |
|---|---|---|
| `POST` | `/budgets` | Create a budget |
| `GET` | `/budgets` | List budgets (paginated, filterable) |
| `GET` | `/budgets/{id}` | Get budget detail |
| `GET` | `/budgets/period?period={YYYY-MM}` | Get budgets for a specific period |
| `PUT` | `/budgets/{id}` | Update a budget |
| `DELETE` | `/budgets/{id}` | Soft-delete a budget |

### Categories

| Method | Path | Description |
|---|---|---|
| `POST` | `/categories` | Create a category |
| `GET` | `/categories` | List all categories for the authenticated user |
| `GET` | `/categories/{id}` | Get category detail |
| `GET` | `/categories/analysis?month={}&year={}` | Spending breakdown by category |
| `PUT` | `/categories/{id}` | Update a category |
| `DELETE` | `/categories/{id}` | Soft-delete a category |

### Response Format

All endpoints return a consistent envelope:

```json
{
  "success": true,
  "message": "Transaction created!",
  "data": { ... }
}
```

Paginated responses wrap content in:

```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  }
}
```

---

## Database Schema

Schema is managed by **Flyway** (`src/main/resources/db/migration/`).

| Migration | Description |
|---|---|
| `V1__init_db.sql` | Core tables: `transactions`, `categories`, `monthly_expenses`, `outbox_messages` |
| `V2__add_budget.sql` | `budgets` table |
| `V3__add-budget-unique.sql` | Unique constraint on budgets |
| `V4__add_threshold_percent.sql` | `threshold_percentage` column on budgets |

**All tables share these base columns:**

| Column | Type | Description |
|---|---|---|
| `id` | UUID | Primary key |
| `user_id` | VARCHAR | Logical tenant identifier (from JWT subject) |
| `created_at` | TIMESTAMP | Auto-set on insert |
| `updated_at` | TIMESTAMP | Auto-set on update |
| `is_deleted` | TIMESTAMP | Soft delete timestamp (null = active) |

**`outbox_messages` columns:**

| Column | Description |
|---|---|
| `module` | Source module name |
| `event_type` | Fully qualified event class name |
| `payload` | JSON-serialized event body |
| `processed_at` | Set when the worker successfully delivers the event |
| `error` | Error message if delivery failed |

---

## Security & Authentication

Authentication is delegated to **Keycloak**. The application acts as an OAuth2 Resource Server — it validates incoming JWTs but issues no tokens itself.

- **Token format:** JWT (RS256)
- **Issuer:** Keycloak realm (`my-realm` by default)
- **Role extraction:** `KeycloakRealmRoleConverter` maps Keycloak realm roles to Spring `GrantedAuthority`
- **User identity:** `principal.getName()` returns the JWT `sub` claim, used as `userId` across all data
- **Session policy:** Stateless — no HTTP session created
- **CSRF:** Disabled (stateless API)

Every request must include:

```
Authorization: Bearer <access_token>
```

---

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)

### Run with Docker Compose

This starts the full stack: PostgreSQL, Keycloak, SonarQube, the application, and Nginx.

```bash
docker-compose up -d
```

| Service | URL |
|---|---|
| Application | http://localhost:8080 |
| Nginx (reverse proxy) | http://localhost:80 |
| Keycloak admin console | http://localhost:9090 |
| SonarQube | http://localhost:9000 |
| PostgreSQL | localhost:5432 |

**Keycloak setup (first run):**
1. Navigate to http://localhost:9090
2. Log in with `admin` / `admin`
3. Create a realm named `my-realm`
4. Create a client for the frontend application
5. Configure the issuer URI to match `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`

### Run Locally

```bash
# 1. Start dependencies only
docker-compose up -d postgres keycloak

# 2. Build the project
./mvnw clean package -DskipTests

# 3. Run the application (uses local profile by default)
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**.

### Run Tests

```bash
./mvnw test
```

### Code Quality

```bash
# Generate JaCoCo coverage report
./mvnw verify

# Run SonarQube analysis (requires SonarQube running on port 9000)
./mvnw sonar:sonar
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/personal_expense_db` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | `http://localhost:9090/realms/my-realm` | Keycloak realm issuer URI |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | `http://localhost:9090/realms/my-realm/protocol/openid-connect/certs` | Keycloak JWKS endpoint |
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile (`local` or `prod`) |

---

## Project Structure

```
personal-expense-be/
├── src/
│   ├── main/
│   │   ├── java/com/huynh/personal_expense_be/
│   │   │   ├── PersonalExpenseBeApplication.java  # Entry point
│   │   │   ├── modules/
│   │   │   │   ├── transaction/
│   │   │   │   │   ├── presentation/              # Controllers
│   │   │   │   │   ├── application/               # Use cases, services, DTOs
│   │   │   │   │   ├── domain/                    # Transaction entity, events
│   │   │   │   │   └── infrastructure/            # JPA adapters, batch, outbox
│   │   │   │   ├── budget/
│   │   │   │   ├── category/
│   │   │   │   └── expense/
│   │   │   └── shared/
│   │   │       ├── config/                        # Security, CORS, async
│   │   │       ├── exception/                     # Global handler, custom exceptions
│   │   │       ├── response/                      # BaseResponse, PaginationResponse
│   │   │       ├── notification/                  # SSE push notifications
│   │   │       └── infrastructure/
│   │   │           ├── persistence/               # Outbox adapter & repository
│   │   │           └── worker/                    # OutboxWorker (15s polling)
│   │   └── resources/
│   │       ├── application.yaml                   # Profile selector
│   │       ├── application-prod.yaml              # All config with env-var overrides
│   │       └── db/migration/                      # Flyway SQL migrations (V1–V4)
│   └── test/
├── nginx/
│   └── nginx.conf                                 # Reverse proxy configuration
├── docker-compose.yml                             # Full stack orchestration
├── Dockerfile                                     # Application container image
├── pom.xml                                        # Maven dependencies & plugins
└── check-style.xml                               # Checkstyle rules
```
