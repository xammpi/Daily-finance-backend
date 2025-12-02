# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Daily Finance Backend — production-ready REST API для отслеживания ежедневных расходов. Приложение предоставляет функционал управления расходами, категориями и балансом пользователя с JWT-аутентификацией.

**For Frontend Developers:** See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for comprehensive API reference with examples.

### Key Features
- User authentication (JWT-based)
- User balance management with deposit/withdraw functionality
- Multi-currency support (20 currencies)
- Expense tracking with automatic balance deduction
- Category management (simplified flat structure)
- Balance summary with monthly expense calculations

### Planned Features
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
│   │   ├── controller/       # REST controllers (4 implemented)
│   │   │   ├── AuthController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── ExpenseController.java
│   │   │   └── UserController.java
│   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── auth/         # LoginRequest, RegisterRequest, AuthResponse
│   │   │   ├── category/     # CategoryRequest, CategoryResponse
│   │   │   ├── expense/      # ExpenseRequest, ExpenseResponse
│   │   │   └── user/         # DepositRequest, UserProfileResponse, BalanceSummaryResponse
│   │   ├── entity/           # JPA entities (3 total + 1 enum)
│   │   │   ├── Category.java
│   │   │   ├── Currency.java (enum)
│   │   │   ├── Expense.java
│   │   │   └── User.java
│   │   ├── exception/        # Exception handling
│   │   │   ├── BadRequestException.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── mapper/           # MapStruct mappers (2 implemented)
│   │   │   ├── CategoryMapper.java
│   │   │   └── ExpenseMapper.java
│   │   ├── repository/       # Spring Data JPA repositories (3 total)
│   │   │   ├── CategoryRepository.java
│   │   │   ├── ExpenseRepository.java
│   │   │   └── UserRepository.java
│   │   ├── security/         # JWT authentication
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── UserPrincipal.java
│   │   └── service/          # Business logic services (4 implemented)
│   │       ├── AuthService.java
│   │       ├── CategoryService.java
│   │       ├── ExpenseService.java
│   │       └── UserService.java
│   └── resources/
│       ├── application.yml                  # Main configuration
│       ├── application-local.yml.template   # Local profile template
│       ├── application-prod.yml             # Production profile
│       └── db/migration/                    # Flyway SQL migrations
│           ├── V1__create_users_table.sql
│           ├── V2__create_accounts_table.sql (deprecated - removed in V8)
│           ├── V3__create_categories_table.sql
│           ├── V4__create_transactions_table.sql (deprecated - removed in V10)
│           ├── V5__create_budgets_table.sql (deprecated - removed in V12)
│           ├── V6__create_recurring_transactions_table.sql (deprecated - removed in V12)
│           ├── V7__refactor_categories_table.sql
│           ├── V8__remove_accounts_add_balance_to_users.sql
│           ├── V11__drop_transactions_and_create_expenses.sql
│           └── V12__drop_budgets_and_recurring_transactions.sql
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

2. **categories** - Expense categories (simplified structure)
   - Fields: id, name, description, user_id, created_at, updated_at
   - Simple flat structure without hierarchy
   - Indexes: user_id
   - Note: V7 migration removed unused fields (type, icon, color, parent_id)

3. **expenses** - Expense tracking records
   - Fields: id, amount, date, description, user_id, category_id, created_at, updated_at
   - Automatically deducts from user balance on create
   - Adjusts balance on update/delete
   - Indexes: user_id, category_id, date, (user_id + date composite)
   - Note: V11 migration created this table; V10 dropped old transactions table

**Entity Relationships:**
- User → Expenses (1:N)
- User → Categories (1:N)
- Category → Expenses (1:N)

### API Architecture
**Style:** RESTful API with JSON payloads
**Base Path:** /api/v1
**Authentication:** Bearer JWT token in Authorization header
**Documentation:** Swagger UI at /swagger-ui.html, OpenAPI spec at /v3/api-docs

**Implemented Endpoints:**

Authentication (Public):
- POST   /api/v1/auth/register     # User registration
- POST   /api/v1/auth/login        # Login, returns JWT

User Profile & Balance (Authenticated):
- GET    /api/v1/user/profile           # Get current user profile
- POST   /api/v1/user/deposit           # Deposit money to balance
- PUT    /api/v1/user/currency          # Update currency preference
- GET    /api/v1/user/balance-summary   # Get balance summary with monthly expenses

Expenses (Authenticated):
- GET    /api/v1/expenses          # List all user expenses
- POST   /api/v1/expenses          # Create expense (deducts from balance)
- GET    /api/v1/expenses/{id}     # Get expense by ID
- PUT    /api/v1/expenses/{id}     # Update expense (adjusts balance)
- DELETE /api/v1/expenses/{id}     # Delete expense (adds back to balance)

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

**6. Balance Management**
- User balance automatically updated when expenses are created/updated/deleted
- Creating expense: balance -= expense.amount
- Updating expense: balance += oldAmount, balance -= newAmount
- Deleting expense: balance += expense.amount
- Deposit operation: balance += deposit.amount
- Service methods marked with @Transactional for ACID compliance
- Rollback on exceptions to maintain data integrity

**7. Monthly Expense Tracking**
- BalanceSummaryResponse provides:
  - currentBalance: User's current balance
  - totalExpensesThisMonth: Sum of all expenses in current calendar month
  - remainingBalance: Current balance (same as currentBalance, since expenses already deducted)
  - currency: User's selected currency

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
- Current version: V12 (latest migration: V12 drops budgets and recurring_transactions tables)

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
- User profile management (get profile, update currency)
- User balance management (deposit money, view balance)
- Multi-currency support (20 currencies, user-selectable)
- Expense tracking with automatic balance deduction
- Monthly expense calculation and balance summary
- Category CRUD (simplified flat structure)
- Database schema with 12 migrations (V1-V12)
- Security configuration with JWT bearer token authentication
- API documentation (Swagger UI)
- Global exception handling
- Maven profiles (local, dev, prod) with environment-specific configurations
- CORS configuration for frontend integration

❌ **Not Yet Implemented:**
- Analytics/statistics endpoints
- CSV export functionality
- Email notifications
- File uploads (receipts/attachments)
- Unit/integration tests

**Recent Major Changes (December 2024):**
- V12 Migration: Removed Budget and RecurringTransaction entities and tables (simplified scope)
- V11 Migration: Consolidated expense table creation (combined V9 and V10 into single migration)
- V8 Migration: Removed Account entity entirely, moved balance/currency to User
- V7 Migration: Simplified Category entity (removed type, icon, color, parent_id)
- Architecture simplification: User → Expenses (instead of User → Accounts → Transactions)
- Expenses only track spending (no INCOME/EXPENSE type), balance managed via deposits
- Added UserService and UserController for balance operations
- Balance summary endpoint shows current balance and monthly expenses
- Removed budget and recurring transaction features to focus on core expense tracking

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
