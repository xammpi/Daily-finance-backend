# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Daily Finance Backend — production-ready REST API для отслеживания ежедневных расходов и доходов. Приложение предоставляет функционал управления транзакциями, категориями, счетами, бюджетами и повторяющимися платежами с JWT-аутентификацией.

### Key Features
User authentication (JWT-based)
Transaction management (expenses/income)
Categories with hierarchy support
Multiple accounts (cards, cash, etc.)
Budgets and recurring transactions/subscriptions
Basic analytics and reporting
CSV export

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
src/
├── main/
│   ├── java/com/expensetracker/
│   │   ├── ExpenseTrackerApplication.java
│   │   ├── config/           # Configuration classes (Security, OpenAPI, MapStruct, CORS)
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data Transfer Objects (request/response)
│   │   ├── entity/           # JPA entities
│   │   ├── exception/        # Custom exceptions and GlobalExceptionHandler
│   │   ├── mapper/           # MapStruct mappers
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JWT filter, UserDetailsService, etc.
│   │   └── service/          # Business logic services
│   └── resources/
│       ├── application.yml           # Main configuration
│       ├── application-local.yml     # Local profile (git-ignored)
│       ├── application-prod.yml      # Production profile
│       ├── db/migration/             # Flyway SQL migrations (V1__, V2__, ...)
│       └── messages.properties       # i18n messages
└── test/
└── java/com/expensetracker/
├── controller/       # Controller integration tests
├── service/          # Service unit tests
└── repository/       # Repository tests with Testcontainers

### Database Schema

Core Entities:
users — User accounts with authentication data
accounts — Financial accounts (bank cards, cash, etc.)
categories — Transaction categories (hierarchical, with parent_id)
transactions — Income/expense records
budgets — Spending limits per category/period
recurring_transactions — Subscriptions and recurring payments
Relationships:
User → Accounts (1:N)
User → Categories (1:N)
Account → Transactions (1:N)
Category → Transactions (1:N)
Category → Category (self-referential for hierarchy)

### API Architecture
Style: RESTful API with JSON payloads
Base Path: /api/v1
Authentication: Bearer JWT token in Authorization header
Documentation: Swagger UI at /swagger-ui.html, OpenAPI spec at /v3/api-docs
Main Endpoints:
POST   /api/v1/auth/register     # User registration
POST   /api/v1/auth/login        # Login, returns JWT
POST   /api/v1/auth/refresh      # Refresh JWT token
GET    /api/v1/transactions      # List transactions (paginated, filterable)
POST   /api/v1/transactions      # Create transaction
GET    /api/v1/transactions/{id} # Get by ID
PUT    /api/v1/transactions/{id} # Update
DELETE /api/v1/transactions/{id} # Delete
GET    /api/v1/categories        # List categories
POST   /api/v1/categories        # Create category
...
GET    /api/v1/accounts          # List user accounts
POST   /api/v1/accounts          # Create account
...
GET    /api/v1/stats/summary     # Dashboard statistics
GET    /api/v1/stats/by-category # Spending by category
GET    /api/v1/export/csv        # Export transactions to CSV

### Key Patterns
1. Layered Architecture
   Controller → Service → Repository → Database
   ↓           ↓
   DTO     Entity/Domain
2. MapStruct for DTO Mapping
   Central config in CentralMappingConfig.java
   Constructor injection strategy
   Null value handling: IGNORE
3. Global Exception Handling
   @ControllerAdvice with GlobalExceptionHandler
   Consistent error response format
   Proper HTTP status codes
4. Validation Strategy
   Request DTOs validated with Jakarta Validation annotations
   Custom validators where needed
   Validation messages externalized to messages.properties
5. Security
   Stateless JWT authentication
   BCrypt password encoding
   CORS configured for frontend origins
   Actuator endpoints secured

## Important Notes

### Configuration
- Never commit application-local.yml — contains secrets
- Use environment variables for sensitive data in production
- JWT secret must be at least 256 bits in production

### Database Migrations
- Migration files are immutable — never edit existing migrations
- Naming convention: V{version}__{description}.sql (e.g., V1__create_users_table.sql)
- Always test migrations on a copy of production data before deploying

### Code Conventions
- Follow SOLID principles
- Use Lombok for boilerplate reduction (but avoid @Data on entities — use @Getter @Setter)
- DTOs are immutable where possible (use records or builders)
- Services handle business logic; controllers are thin
- All public service methods should be unit tested

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
