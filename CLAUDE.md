# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Daily Finance Backend — production-ready REST API для отслеживания ежедневных расходов и доходов. Приложение предоставляет функционал управления транзакциями, категориями, счетами, бюджетами и повторяющимися платежами с JWT-аутентификацией.

### Key Features
- User authentication (JWT-based)
- Transaction management (expenses/income)
- Categories with hierarchy support
- Multiple accounts (cards, cash, etc.)

### Planned Features (Entities exist, endpoints not yet implemented)
- Budgets and recurring transactions
- Analytics and reporting
- CSV export

## Development Commands

### Setup
# Clone and setup
git clone <repository-url>
cd daily-finance-backend
# Create local configuration (copy from template)
cp src/main/resources/application-local.yml.template src/main/resources/application-local.yml
# Install dependencies and build
./mvnw clean install

### Running the Application
# Development mode with local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# Or using Makefile
make run
# Production mode
java -jar target/expense-tracker-api-1.0.0.jar --spring.profiles.active=prod

### Testing
# Run all tests
./mvnw test
# Run with coverage
./mvnw test jacoco:report
# Run specific test class
./mvnw test -Dtest=ExpenseServiceTest
# Run integration tests
./mvnw verify -P integration-test

### Database
# Flyway migrations run automatically on startup
# Manual migration commands:
# Validate migrations
./mvnw flyway:validate
# View migration info
./mvnw flyway:info
# Repair migration history (if needed)
./mvnw flyway:repair
# Clean database (CAUTION: drops all objects)
./mvnw flyway:clean -Dflyway.cleanDisabled=false

### Build & Deploy
# Build without tests
./mvnw clean package -DskipTests
# Build with tests
./mvnw clean package
# Build Docker image
docker build -t expense-tracker-api:latest .
# Run with Docker Compose
docker-compose up -d
# View logs
docker-compose logs -f app

## Architecture

### Technology Stack
| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.2.x |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Security | Spring Security + JWT (jjwt 0.12.x) |
| Mapping | MapStruct 1.5.x |
| Validation | Jakarta Validation |
| API Docs | SpringDoc OpenAPI 2.3.x |
| Monitoring | Spring Actuator + Micrometer + Prometheus |
| Testing | JUnit 5, Testcontainers, H2 |
| Build | Maven |

### Project Structure
```
src/
├── main/
│   ├── java/com/expensetracker/
│   │   ├── ExpenseTrackerApplication.java
│   │   ├── config/           # Configuration classes
│   │   │   ├── CentralMappingConfig.java    # MapStruct configuration
│   │   │   ├── CorsConfig.java              # CORS configuration
│   │   │   ├── OpenApiConfig.java           # Swagger/OpenAPI setup
│   │   │   └── SecurityConfig.java          # Spring Security + JWT
│   │   ├── controller/       # REST controllers (4 implemented)
│   │   │   ├── AccountController.java
│   │   │   ├── AuthController.java
│   │   │   ├── CategoryController.java
│   │   │   └── TransactionController.java
│   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── account/      # AccountRequest, AccountResponse
│   │   │   ├── auth/         # LoginRequest, RegisterRequest, AuthResponse
│   │   │   ├── budget/       # BudgetRequest, BudgetResponse (not yet used)
│   │   │   ├── category/     # CategoryRequest, CategoryResponse
│   │   │   └── transaction/  # TransactionRequest, TransactionResponse
│   │   ├── entity/           # JPA entities (6 total)
│   │   │   ├── Account.java
│   │   │   ├── Budget.java
│   │   │   ├── Category.java
│   │   │   ├── RecurringTransaction.java
│   │   │   ├── Transaction.java
│   │   │   └── User.java
│   │   ├── exception/        # Exception handling
│   │   │   ├── BadRequestException.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── mapper/           # MapStruct mappers (4 implemented)
│   │   │   ├── AccountMapper.java
│   │   │   ├── BudgetMapper.java (entity mapper ready)
│   │   │   ├── CategoryMapper.java
│   │   │   └── TransactionMapper.java
│   │   ├── repository/       # Spring Data JPA repositories (6 total)
│   │   │   ├── AccountRepository.java
│   │   │   ├── BudgetRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   ├── RecurringTransactionRepository.java
│   │   │   ├── TransactionRepository.java
│   │   │   └── UserRepository.java
│   │   ├── security/         # JWT authentication
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── UserPrincipal.java
│   │   └── service/          # Business logic services (4 implemented)
│   │       ├── AccountService.java
│   │       ├── AuthService.java
│   │       ├── CategoryService.java
│   │       └── TransactionService.java
│   └── resources/
│       ├── application.yml                  # Main configuration
│       ├── application-local.yml.template   # Local profile template
│       ├── application-prod.yml             # Production profile
│       ├── db/migration/                    # Flyway SQL migrations
│       │   ├── V1__create_users_table.sql
│       │   ├── V2__create_accounts_table.sql
│       │   ├── V3__create_categories_table.sql
│       │   ├── V4__create_transactions_table.sql
│       │   ├── V5__create_budgets_table.sql
│       │   └── V6__create_recurring_transactions_table.sql
│       └── messages.properties              # Validation messages
└── test/                    # Test structure (to be implemented)
    └── java/com/expensetracker/
```

### Database Schema

**Core Entities (All tables created via Flyway migrations):**

1. **users** - User accounts with authentication data
   - Fields: id, email, username, password (BCrypt), first_name, last_name, enabled, created_at, updated_at
   - Indexes: email, username (unique)

2. **accounts** - Financial accounts (bank cards, cash, etc.)
   - Fields: id, name, type (enum), balance, currency, description, active, user_id, created_at, updated_at
   - Types: CASH, BANK_ACCOUNT, CREDIT_CARD, DEBIT_CARD, SAVINGS, INVESTMENT, OTHER
   - Indexes: user_id, type, active

3. **categories** - Transaction categories (hierarchical, with parent_id)
   - Fields: id, name, type (INCOME/EXPENSE), icon, color, description, parent_id, user_id, created_at, updated_at
   - Supports nested categories via self-referential parent_id
   - Indexes: user_id, type, parent_id

4. **transactions** - Income/expense records
   - Fields: id, amount, type (INCOME/EXPENSE), date, description, notes, account_id, category_id, created_at, updated_at
   - Automatically updates account balance on create/update/delete
   - Indexes: account_id, category_id, date, type, (date + type composite)

5. **budgets** - Spending limits per category/period
   - Fields: id, name, amount, period (enum), start_date, end_date, active, category_id, user_id, created_at, updated_at
   - Periods: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
   - Indexes: user_id, category_id, active, period
   - Note: Entity and repository exist, endpoints not yet implemented

6. **recurring_transactions** - Subscriptions and recurring payments
   - Fields: id, name, amount, type, frequency, start_date, end_date, next_occurrence, active, description, user_id, account_id, category_id, created_at, updated_at
   - Frequency: DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
   - Indexes: user_id, active, next_occurrence
   - Note: Entity and repository exist, endpoints not yet implemented

**Entity Relationships:**
- User → Accounts (1:N)
- User → Categories (1:N)
- User → Budgets (1:N)
- User → RecurringTransactions (1:N)
- Account → Transactions (1:N)
- Category → Transactions (1:N)
- Category → Budgets (1:N)
- Category → Category (self-referential for hierarchy)

### API Architecture
**Style:** RESTful API with JSON payloads
**Base Path:** /api/v1
**Authentication:** Bearer JWT token in Authorization header
**Documentation:** Swagger UI at /swagger-ui.html, OpenAPI spec at /v3/api-docs

**Implemented Endpoints:**

Authentication (Public):
- POST   /api/v1/auth/register     # User registration
- POST   /api/v1/auth/login        # Login, returns JWT

Transactions (Authenticated):
- GET    /api/v1/transactions      # List transactions (paginated, sorted by date DESC)
- POST   /api/v1/transactions      # Create transaction (updates account balance)
- GET    /api/v1/transactions/{id} # Get transaction by ID
- PUT    /api/v1/transactions/{id} # Update transaction
- DELETE /api/v1/transactions/{id} # Delete transaction

Categories (Authenticated):
- GET    /api/v1/categories        # List all user categories
- POST   /api/v1/categories        # Create category (supports parent_id for hierarchy)
- GET    /api/v1/categories/{id}   # Get category by ID
- PUT    /api/v1/categories/{id}   # Update category
- DELETE /api/v1/categories/{id}   # Delete category

Accounts (Authenticated):
- GET    /api/v1/accounts          # List all user accounts
- POST   /api/v1/accounts          # Create account
- GET    /api/v1/accounts/{id}     # Get account by ID
- PUT    /api/v1/accounts/{id}     # Update account
- DELETE /api/v1/accounts/{id}     # Delete account

### Key Patterns & Implementation Details

**1. Layered Architecture**
```
Controller → Service → Repository → Database
   ↓           ↓
  DTO     Entity/Domain
```
- Controllers: Thin, handle HTTP requests/responses only
- Services: Business logic, transaction management, validation
- Repositories: Spring Data JPA interfaces with custom queries
- Entities: JPA entities with Lombok annotations

**2. MapStruct for DTO Mapping**
- Central config in `CentralMappingConfig.java`
- Component model: Spring (for dependency injection)
- Injection strategy: Constructor
- Null value handling: IGNORE
- All mappers implement bidirectional mapping (toEntity, toResponse, updateEntityFromRequest)

**3. Global Exception Handling**
- `@ControllerAdvice` with `GlobalExceptionHandler`
- Consistent `ErrorResponse` format (timestamp, status, error, message, path, details)
- Handles: ResourceNotFoundException (404), BadRequestException (400), ValidationException (400), AuthenticationException (401)
- All exceptions return proper HTTP status codes

**4. Validation Strategy**
- Request DTOs validated with Jakarta Validation annotations (@NotNull, @NotBlank, @Email, @Positive, etc.)
- Validation triggers automatically via @Valid in controllers
- Validation messages externalized to `messages.properties`
- Field-level validation with detailed error responses

**5. Security Implementation**
- **Authentication**: Stateless JWT (no sessions)
- **Password**: BCrypt encoding
- **Token Generation**: JwtTokenProvider with configurable expiration
- **Filter**: JwtAuthenticationFilter runs before UsernamePasswordAuthenticationFilter
- **CORS**: Configured for localhost origins (frontend integration)
- **Public Endpoints**: /api/v1/auth/**, /actuator/health, /swagger-ui/**, /v3/api-docs/**
- **Secured Endpoints**: All other /api/v1/** routes require Bearer token

**6. Transaction Management**
- Account balance automatically updated when transactions are created/updated/deleted
- Service methods marked with @Transactional for ACID compliance
- Rollback on exceptions to maintain data integrity

## Important Notes

### Configuration
- **Never commit** `application-local.yml` — it contains secrets (git-ignored by default)
- Use environment variables for sensitive data in production (`JWT_SECRET`, `DATABASE_URL`, etc.)
- JWT secret must be at least 256 bits (32 characters) in production
- Default JWT expiration: 24 hours (access token), 7 days (refresh token)

### Database Migrations
- Migration files are **immutable** — never edit existing migrations
- Naming convention: `V{version}__{description}.sql` (e.g., `V1__create_users_table.sql`)
- Migrations run automatically on startup (Flyway enabled)
- Always test migrations on a copy of production data before deploying
- Current version: V6 (6 tables created)

### Code Conventions
- **SOLID principles**: Single responsibility, dependency injection via constructor
- **Lombok usage**: Use `@Getter @Setter` on entities (avoid `@Data`), `@RequiredArgsConstructor` for DI
- **DTOs**: Prefer immutable where possible; use validation annotations
- **Services**: Business logic only; controllers stay thin
- **Error handling**: Throw domain exceptions (BadRequestException, ResourceNotFoundException)
- **Security**: Always check user ownership before allowing CRUD operations
- **Testing**: All public service methods should be unit tested (test structure exists, tests to be implemented)

### Current Implementation Status
✅ **Fully Implemented:**
- User authentication (register, login with JWT)
- Transaction CRUD with account balance management
- Account CRUD
- Category CRUD with hierarchy support
- Database schema (all 6 tables)
- Security configuration
- API documentation (Swagger UI)
- Global exception handling

⏳ **Partially Implemented (Entities/Repositories exist, no endpoints):**
- Budgets
- Recurring transactions

❌ **Not Yet Implemented:**
- Analytics/statistics endpoints
- CSV export functionality
- Budget management endpoints
- Recurring transaction management endpoints
- Email notifications
- File uploads (receipts/attachments)
- Unit/integration tests

### Common Commands Quick Reference
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local  # Run locally
./mvnw test                                               # Run tests
./mvnw clean package -DskipTests                          # Build JAR
docker-compose up -d                                      # Start with Docker
```

### Useful URLs (local development)
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health
- Prometheus metrics: http://localhost:8080/actuator/prometheus
