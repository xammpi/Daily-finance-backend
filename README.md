# Daily Finance Backend

Production-ready REST API for tracking daily expenses with JWT authentication, multi-currency support, and advanced search capabilities.

## Quick Start

```bash
# Setup
./mvnw clean install

# Run
./mvnw spring-boot:run

# Access
http://localhost:8080
http://localhost:8080/swagger-ui.html
```

## Features

- 🔐 JWT Authentication
- 💰 Multi-currency wallet management (20 currencies)
- 📊 Expense tracking with categories
- 🔍 Advanced search & filter system
- 📈 Comprehensive statistics (daily, weekly, monthly)
- ⚡ Generic pagination & sorting
- 🏗️ Rich domain model architecture
- 📝 Complete field validation

## Tech Stack

- **Framework:** Spring Boot 3.2.x
- **Language:** Java 21
- **Database:** PostgreSQL 16
- **Security:** Spring Security + JWT
- **ORM:** Spring Data JPA
- **Migrations:** Flyway
- **Mapping:** MapStruct
- **Docs:** OpenAPI/Swagger

## Documentation

- **[API Guide](API_GUIDE.md)** - Complete API reference with examples
- **[CLAUDE.md](CLAUDE.md)** - Development guide for Claude Code
- **Swagger UI** - Interactive API docs at `/swagger-ui.html`

## Quick Commands

```bash
# Development
./mvnw spring-boot:run              # Run application
./mvnw test                         # Run tests
./mvnw clean compile                # Compile

# Database
./mvnw flyway:info                  # Migration status
./mvnw flyway:validate              # Validate migrations

# Build
./mvnw clean package -DskipTests    # Build JAR
docker-compose up -d                # Run with Docker
```

## API Endpoints

### Authentication (Public)
- `POST /api/v1/auth/register` - Register user
- `POST /api/v1/auth/login` - Login

### Currencies (Public)
- `GET /api/v1/currencies` - Get all currencies

### User & Wallet
- `GET /api/v1/user/profile` - Get profile
- `GET /api/v1/user/wallet` - Get wallet details
- `POST /api/v1/user/deposit` - Deposit money
- `POST /api/v1/user/withdraw` - Withdraw money

### Categories
- `POST /api/v1/categories` - Create category
- `POST /api/v1/categories/search` - Advanced search

### Expenses
- `POST /api/v1/expenses` - Create transaction
- `POST /api/v1/expenses/search` - Advanced search
- `GET /api/v1/expenses/statistics` - Get statistics

See **[API_GUIDE.md](API_GUIDE.md)** for complete endpoint reference.

## Configuration

1. Copy template: `cp src/main/resources/application-local.yml.template src/main/resources/application-local.yml`
2. Configure database and JWT secret
3. Run: `./mvnw spring-boot:run`

## License

Proprietary
