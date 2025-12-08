# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Daily Finance Backend — production-ready REST API для отслеживания ежедневных расходов. Приложение предоставляет функционал управления расходами, категориями и балансом пользователя с JWT-аутентификацией.

**For Frontend Developers:** See [API_GUIDE.md](API_GUIDE.md) for comprehensive API reference with examples.

### Key Features
- User authentication (JWT-based)
- Wallet management with multi-currency support
- Transaction tracking (both income and expenses)
- Multi-currency support (20 currencies)
- Transaction tracking with automatic wallet updates
- Balance validation (prevents negative balance)
- Category management with type (INCOME/EXPENSE)
- Comprehensive transaction statistics:
  - Today, week, and month expenses
  - Total expenses (all time)
  - Average daily/weekly/monthly spending
  - Comparison with previous periods
  - Category-wise breakdowns with percentages
- Low balance warnings
- Detailed wallet information with transaction summaries

### Planned Features
- CSV export
- Customizable low balance thresholds

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
java -jar target/transaction-tracker-api-1.0.0.jar --spring.profiles.active=prod

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
docker build -t transaction-tracker-api:latest .
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
│   │   ├── controller/       # REST controllers (5 implemented)
│   │   │   ├── AuthController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── CurrencyController.java
│   │   │   ├── TransactionController.java
│   │   │   └── UserController.java
│   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── auth/         # LoginRequest, RegisterRequest, AuthResponse
│   │   │   ├── category/     # CategoryRequest, CategoryResponse
│   │   │   ├── common/       # Generic pagination & search DTOs (FilterRequest, PagedResponse, etc.)
│   │   │   ├── currency/     # CurrencyResponse
│   │   │   ├── transaction/  # TransactionRequest, TransactionResponse, TransactionStatisticsResponse
│   │   │   └── user/         # UserProfileResponse, WalletResponse
│   │   ├── entity/           # JPA entities (6 total + 1 enum)
│   │   │   ├── BaseEntity.java
│   │   │   ├── Category.java
│   │   │   ├── CategoryType.java (enum: INCOME, EXPENSE)
│   │   │   ├── Currency.java
│   │   │   ├── Transaction.java
│   │   │   ├── User.java
│   │   │   └── Wallet.java
│   │   ├── exception/        # Exception handling
│   │   │   ├── BadRequestException.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── mapper/           # MapStruct mappers (3 implemented)
│   │   │   ├── CategoryMapper.java
│   │   │   ├── CurrencyMapper.java
│   │   │   └── TransactionMapper.java
│   │   ├── repository/       # Spring Data JPA repositories (5 total)
│   │   │   ├── CategoryRepository.java
│   │   │   ├── CurrencyRepository.java
│   │   │   ├── TransactionRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── WalletRepository.java
│   │   ├── security/         # JWT authentication
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── UserPrincipal.java
│   │   ├── service/          # Business logic services (5 implemented)
│   │   │   ├── AuthService.java
│   │   │   ├── CategoryService.java
│   │   │   ├── CurrencyService.java
│   │   │   ├── TransactionService.java
│   │   │   └── UserService.java
│   │   └── specification/    # Generic search specification builders
│   │       ├── GenericSpecification.java
│   │       └── SpecificationBuilder.java
│   └── resources/
│       ├── application.yml                  # Main configuration
│       ├── application-local.yml.template   # Local profile template
│       ├── application-prod.yml             # Production profile
│       └── db/migration/                    # Flyway SQL migrations
│           ├── V1__create_users_table.sql
│           ├── V2__create_accounts_table.sql (deprecated - removed in V8)
│           ├── V3__create_categories_table.sql
│           ├── V4__create_transactions_table.sql (deprecated - removed in V11)
│           ├── V5__create_budgets_table.sql (deprecated - removed in V12)
│           ├── V6__create_recurring_transactions_table.sql (deprecated - removed in V12)
│           ├── V7__refactor_categories_table.sql
│           ├── V8__remove_accounts_add_balance_to_users.sql (deprecated - replaced by V14)
│           ├── V11__drop_transactions_and_create_expenses.sql
│           ├── V12__drop_budgets_and_recurring_transactions.sql
│           ├── V13__create_currencies_table.sql
│           ├── V14__create_wallets_and_migrate_balance.sql
│           ├── V15__create_deposits_table.sql
│           ├── V16_create_wallets_for_existing_users.sql
│           ├── V17__add_mdl_currency.sql
│           ├── V18__rename_expanses_table_to_transactions.sql
│           ├── V19__add_transactional_type.sql (deprecated - removed in V21)
│           ├── V20__add_transactional_type_to_categories.sql
│           └── V21__remove_transacation_type_from_transactions-table.sql
└── test/                    # Test structure (to be implemented)
    └── java/com/expensetracker/
```

### Database Schema

**Core Entities (All tables created via Flyway migrations):**

1. **users** - User accounts with authentication
   - Fields: id, email, username, password (BCrypt), first_name, last_name, enabled, created_at, updated_at
   - Indexes: email, username (unique)
   - Note: balance and currency moved to wallets table in V14

2. **wallets** - User wallet with balance and currency
   - Fields: id, amount, user_id (unique), currency_id, created_at, updated_at
   - One-to-one relationship with users
   - Automatically updated by transactions
   - Indexes: user_id, currency_id
   - Created in V14 migration

3. **currencies** - Available currencies
   - Fields: id, code (unique), name, symbol, created_at, updated_at
   - Pre-populated with 20 currencies (USD, EUR, GBP, JPY, CNY, RUB, UAH, PLN, CHF, CAD, AUD, BRL, INR, KRW, MXN, SEK, NOK, DKK, TRY, ZAR)
   - MDL added in V17
   - Indexes: code
   - Created in V13 migration

4. **categories** - Transaction categories (INCOME or EXPENSE)
   - Fields: id, name, description, type (INCOME/EXPENSE), user_id, created_at, updated_at
   - Flat structure without hierarchy
   - Each category has a type (CategoryType enum)
   - Indexes: user_id
   - V20 migration added type field

5. **transactions** - Transaction records (income or expenses)
   - Fields: id, amount, date, description, user_id, category_id, created_at, updated_at
   - Automatically updates wallet balance based on category type
   - EXPENSE categories: deduct from wallet
   - INCOME categories: add to wallet
   - Indexes: user_id, category_id, date, (user_id + date composite)
   - V18 migration renamed from expenses to transactions

6. **deposits** - Deposit records
   - Fields: id, amount, date, description, user_id, created_at, updated_at
   - Tracks deposit history
   - Created in V15 migration

**Entity Relationships:**
- User → Wallet (1:1)
- Wallet → Currency (N:1)
- User → Transactions (1:N)
- User → Categories (1:N)
- User → Deposits (1:N)
- Category → Transactions (1:N)

### API Architecture
**Style:** RESTful API with JSON payloads
**Base Path:** /api/v1
**Authentication:** Bearer JWT token in Authorization header
**Documentation:** Swagger UI at /swagger-ui.html, OpenAPI spec at /v3/api-docs

**Implemented Endpoints:**

Authentication (Public):
- POST   /api/v1/auth/register     # User registration
- POST   /api/v1/auth/login        # Login, returns JWT

Currencies (Public):
- GET    /api/v1/currencies             # Get all available currencies

User Profile & Wallet (Authenticated):
- GET    /api/v1/user/profile           # Get current user profile
- GET    /api/v1/user/wallet            # Get detailed wallet info (balance, currency)
- PUT    /api/v1/user/currency          # Update currency preference

Transactions (Authenticated):
- POST   /api/v1/transactions                  # Create transaction (with balance validation)
- GET    /api/v1/transactions/{id}             # Get transaction by ID
- PUT    /api/v1/transactions/{id}             # Update transaction (adjusts balance)
- DELETE /api/v1/transactions/{id}             # Delete transaction (adjusts balance)
- GET    /api/v1/transactions/statistics       # Get comprehensive transaction statistics
- POST   /api/v1/transactions/search           # Advanced search with dynamic criteria

Categories (Authenticated):
- POST   /api/v1/categories        # Create category (name, description)
- GET    /api/v1/categories/{id}   # Get category by ID
- PUT    /api/v1/categories/{id}   # Update category
- DELETE /api/v1/categories/{id}   # Delete category
- POST   /api/v1/categories/search # Advanced search with dynamic criteria (NEW)

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

**6. Wallet & Balance Management**
- Wallet balance automatically updated when transactions are created/updated/deleted
- Transactions can be INCOME or EXPENSE based on category type
- **Creating EXPENSE transaction**: Validates sufficient balance, then deducts from wallet
- **Creating INCOME transaction**: Adds to wallet balance
- **Updating transaction**: Reverses old amount, applies new amount based on category type
- **Deleting transaction**: Reverses transaction effect on wallet
- **Balance Validation**: EXPENSE transactions check for sufficient funds before creation
- Service methods marked with @Transactional for ACID compliance
- Rollback on exceptions to maintain data integrity
- Each user has one wallet with a selected currency

**7. Transaction Statistics & Analytics**
- **TransactionStatisticsResponse** provides comprehensive statistics:
  - todayTransactions, weekTransactions, monthTransactions
  - totalTransactions: All-time total
  - averageDailyTransactions, averageWeeklyTransactions, averageMonthlyTransactions
  - previousWeekTransactions, previousMonthTransactions: For comparison
  - currency: User's selected currency

- **WalletResponse** provides wallet information:
  - Wallet ID
  - Current balance
  - Currency (full Currency entity with code, name, symbol)

**8. Generic Search & Filter Framework**
- **Universal approach** that works with ANY entity (Transaction, Category, future entities)
- **Architecture:**
  - `GenericSpecification<T>` - Converts SearchCriteria to JPA Specification
  - `SpecificationBuilder` - Combines multiple specifications with AND/OR logic
  - `FilterRequest` - Generic DTO containing criteria, pagination, sorting
  - `SearchOperation` enum - 14 operations (EQUALS, LIKE, BETWEEN, etc.)
- **Features:**
  - Dynamic query building using JPA Criteria API (no SQL injection risk)
  - Nested field support (e.g., search by "category.name" in Transaction)
  - Automatic type conversion (String → Long, LocalDate, BigDecimal, etc.)
  - Pagination with configurable size (max 100)
  - Flexible sorting (any field, ASC/DESC)
  - Multi-tenant security (automatic user filtering)
- **Usage in services:**
  - Build Specification from FilterRequest criteria
  - Add user filter for security
  - Execute with repository.findAll(spec, pageable)
  - Map results to DTOs
- **Extensibility:** Adding search to new entities requires minimal code
- **Frontend integration:** See API_GUIDE.md for complete examples

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
- Current version: V21 (latest migration: V21 removes transaction type from transactions table)

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
- Wallet management:
  - One wallet per user with balance and currency
  - Automatic wallet creation for new users
  - Currency selection from 21 available currencies
  - Detailed wallet information endpoint
- Multi-currency support (21 currencies, user-selectable, public endpoint to get all currencies)
- Transaction tracking:
  - Create, read, update, delete transactions
  - Support for both INCOME and EXPENSE transactions
  - Automatic wallet updates based on transaction type
  - Balance validation (prevents negative balance on EXPENSE transactions)
- Category management:
  - CRUD operations for categories
  - Categories have types (INCOME or EXPENSE)
  - Flat structure (no hierarchy)
  - User-specific categories
- Comprehensive analytics and statistics:
  - Transaction statistics (today, week, month totals)
  - All-time totals
  - Average daily/weekly/monthly spending
  - Comparisons with previous periods
- **Generic Search & Filter Framework**:
  - Works with ANY entity (Transaction, Category, future entities)
  - 14 search operations (EQUALS, LIKE, GREATER_THAN, BETWEEN, etc.)
  - Dynamic criteria building with JPA Specifications
  - Nested field support (e.g., category.name)
  - Pagination and sorting
  - Type-safe with automatic conversion
  - See: `API_GUIDE.md` for complete search documentation
- Database schema with 21 migrations (V1-V21)
- Security configuration with JWT bearer token authentication
- API documentation (Swagger UI)
- Global exception handling
- Maven profiles (local, dev, prod) with environment-specific configurations
- CORS configuration for frontend integration
- Rich domain models with validation

❌ **Not Yet Implemented:**
- CSV export functionality
- Email notifications
- File uploads (receipts/attachments)
- Unit/integration tests
- Customizable low balance thresholds

**Recent Major Changes (December 2024):**
- **REFACTORED**: Renamed Expense → Transaction (V18)
- **NEW**: Added CategoryType enum (INCOME, EXPENSE) to categories (V20)
- **NEW**: Transactions now support both INCOME and EXPENSE based on category type
- **NEW**: Created separate Wallet entity (V14) - moved balance from User to Wallet
- **NEW**: Added Currency entity and table (V13) with 21 pre-populated currencies
- **NEW**: Added Deposits tracking table (V15)
- **NEW**: Comprehensive transaction statistics with averages and comparisons
- **NEW**: Generic Search & Filter Framework for dynamic queries
- **CLEANED**: Removed dead code (Deposit entity, DepositRepository, unused DTOs, unused methods)
- **REMOVED**: Transaction type field from transactions table (V21) - type now determined by category
- V12 Migration: Removed Budget and RecurringTransaction entities and tables
- V7 Migration: Simplified Category entity (removed icon, color, parent_id)
- Architecture: User → Wallet (1:1), User → Transactions (1:N), Category → Transactions (1:N)
- Balance management: Automatic wallet updates based on transaction category type
- Removed budget and recurring transaction features to focus on core transaction tracking
- Code cleanup: Removed unused imports, methods, and constants

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
