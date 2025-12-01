# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Daily Finance Backend — production-ready REST API для отслеживания ежедневных расходов и доходов. Приложение предоставляет функционал управления транзакциями, категориями, балансом пользователя и повторяющимися платежами с JWT-аутентификацией.

### Key Features
- User authentication (JWT-based)
- User balance management with multi-currency support (20 currencies)
- Transaction management (expenses/income) with automatic balance updates
- Category management (simplified structure)

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
# Development mode with local profile (default)
./mvnw spring-boot:run
# Or explicitly specify the local profile
./mvnw spring-boot:run -Plocal
# Development profile
./mvnw spring-boot:run -Pdev
# Production profile
./mvnw spring-boot:run -Pprod
# Or run the built JAR with a specific profile
java -jar target/expense-tracker-api-1.0.0.jar --spring.profiles.active=prod

**Note:** Maven profiles are configured in pom.xml (local, dev, prod). The local profile is active by default for development.

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
# Build without tests (uses default local profile)
./mvnw clean package -DskipTests
# Build with tests
./mvnw clean package
# Build with specific profile
./mvnw clean package -Pprod -DskipTests
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
│   │   ├── controller/       # REST controllers (3 implemented)
│   │   │   ├── AuthController.java
│   │   │   ├── CategoryController.java
│   │   │   └── TransactionController.java
│   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── auth/         # LoginRequest, RegisterRequest, AuthResponse
│   │   │   ├── budget/       # BudgetRequest, BudgetResponse (not yet used)
│   │   │   ├── category/     # CategoryRequest, CategoryResponse
│   │   │   └── transaction/  # TransactionRequest, TransactionResponse
│   │   ├── entity/           # JPA entities (5 total + 1 enum)
│   │   │   ├── Budget.java
│   │   │   ├── Category.java
│   │   │   ├── Currency.java (enum)
│   │   │   ├── RecurringTransaction.java
│   │   │   ├── Transaction.java
│   │   │   └── User.java
│   │   ├── exception/        # Exception handling
│   │   │   ├── BadRequestException.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── mapper/           # MapStruct mappers (3 implemented)
│   │   │   ├── BudgetMapper.java (entity mapper ready)
│   │   │   ├── CategoryMapper.java
│   │   │   └── TransactionMapper.java
│   │   ├── repository/       # Spring Data JPA repositories (5 total)
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
│   │   └── service/          # Business logic services (3 implemented)
│   │       ├── AuthService.java
│   │       ├── CategoryService.java
│   │       └── TransactionService.java
│   └── resources/
│       ├── application.yml                  # Main configuration
│       ├── application-local.yml.template   # Local profile template
│       ├── application-prod.yml             # Production profile
│       └── db/migration/                    # Flyway SQL migrations
│           ├── V1__create_users_table.sql
│           ├── V2__create_accounts_table.sql (deprecated)
│           ├── V3__create_categories_table.sql
│           ├── V4__create_transactions_table.sql
│           ├── V5__create_budgets_table.sql
│           ├── V6__create_recurring_transactions_table.sql
│           ├── V7__refactor_categories_table.sql
│           └── V8__remove_accounts_add_balance_to_users.sql
└── test/                    # Test structure (to be implemented)
    └── java/com/expensetracker/
```

### Database Schema

**Core Entities (All tables created via Flyway migrations):**

1. **users** - User accounts with authentication and balance management
   - Fields: id, email, username, password (BCrypt), first_name, last_name, enabled, balance, currency, created_at, updated_at
   - Indexes: email, username (unique)
   - V8 migration added: balance (default 0.00), currency (default USD)
   - Currency enum: USD, EUR, GBP, JPY, CNY, RUB, UAH, PLN, CHF, CAD, AUD, BRL, INR, KRW, MXN, SEK, NOK, DKK, TRY, ZAR

2. **categories** - Transaction categories (simplified structure)
   - Fields: id, name, description, user_id, created_at, updated_at
   - Simple flat structure without hierarchy
   - Indexes: user_id
   - Note: V7 migration removed unused fields (type, icon, color, parent_id)

3. **transactions** - Income/expense records
   - Fields: id, amount, type (INCOME/EXPENSE), date, description, notes, user_id, category_id, created_at, updated_at
   - Automatically updates user balance on create/update/delete
   - Indexes: user_id, category_id, date, type, (date + type composite)
   - Note: V8 migration changed account_id to user_id

4. **budgets** - Spending limits per category/period
   - Fields: id, name, amount, period (enum), start_date, end_date, active, category_id, user_id, created_at, updated_at
   - Periods: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
   - Indexes: user_id, category_id, active, period
   - Note: Entity and repository exist, endpoints not yet implemented

5. **recurring_transactions** - Subscriptions and recurring payments
   - Fields: id, name, amount, type, frequency, start_date, end_date, next_occurrence, active, description, user_id, category_id, created_at, updated_at
   - Frequency: DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
   - Indexes: user_id, active, next_occurrence
   - Note: Entity and repository exist, endpoints not yet implemented

**Entity Relationships:**
- User → Transactions (1:N)
- User → Categories (1:N)
- User → Budgets (1:N)
- User → RecurringTransactions (1:N)
- Category → Transactions (1:N)
- Category → Budgets (1:N)

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
- POST   /api/v1/categories        # Create category (name, description)
- GET    /api/v1/categories/{id}   # Get category by ID
- PUT    /api/v1/categories/{id}   # Update category
- DELETE /api/v1/categories/{id}   # Delete category

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
- Field-level validation with detailed error responses
- Default validation messages used (no custom messages.properties file)

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
- **Maven Profiles**: Three profiles available in pom.xml:
  - `local` (default) - For local development
  - `dev` - For development environment
  - `prod` - For production environment
- **Never commit** `application-local.yml` — it contains secrets (git-ignored by default)
- **Never commit** `.claude/` — contains local Claude Code settings (git-ignored)
- Use environment variables for sensitive data in production (`JWT_SECRET`, `DATABASE_URL`, etc.)
- JWT secret must be at least 256 bits (32 characters) in production
- Default JWT expiration: 24 hours (access token), 7 days (refresh token)
- Error message configuration: `server.error.include-message: always` (Spring Boot 3.x standard)

### Database Migrations
- Migration files are **immutable** — never edit existing migrations
- Naming convention: `V{version}__{description}.sql` (e.g., `V1__create_users_table.sql`)
- Migrations run automatically on startup (Flyway enabled)
- Always test migrations on a copy of production data before deploying
- Current version: V8 (latest: remove accounts table, add balance/currency to users)

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
- User balance management with multi-currency support (20 currencies)
- Transaction CRUD with automatic user balance updates
- Category CRUD (simplified flat structure)
- Database schema with 8 migrations (V1-V8)
- Security configuration with JWT bearer token authentication
- API documentation (Swagger UI)
- Global exception handling
- Maven profiles (local, dev, prod) with environment-specific configurations
- CORS configuration for frontend integration

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

**Recent Major Changes:**
- V8 Migration (2024): Removed Account entity entirely, moved balance/currency to User
- V7 Migration (2024): Simplified Category entity (removed type, icon, color, parent_id)
- Transactions now directly update User balance instead of Account balance

### Common Commands Quick Reference
```bash
./mvnw spring-boot:run              # Run with local profile (default)
./mvnw spring-boot:run -Pdev        # Run with dev profile
./mvnw spring-boot:run -Pprod       # Run with prod profile
./mvnw test                         # Run tests
./mvnw clean package -DskipTests    # Build JAR
./mvnw clean package -Pprod         # Build JAR with prod profile
docker-compose up -d                # Start with Docker
```

### Useful URLs (local development)
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health
- Prometheus metrics: http://localhost:8080/actuator/prometheus
