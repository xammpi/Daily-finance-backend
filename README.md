# Daily Finance Backend

Production-ready REST API for tracking daily expenses and income.

## Features

- User authentication (JWT-based)
- Transaction management (expenses/income)
- Categories with hierarchy support
- Multiple accounts (cards, cash, etc.)
- Budgets and recurring transactions
- RESTful API with OpenAPI documentation

## Tech Stack

- Java 21
- Spring Boot 3.2.x
- PostgreSQL 16
- Spring Security + JWT
- MapStruct
- Flyway
- OpenAPI 3.0

## Quick Start

### Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 16

### Setup

1. Clone the repository
2. Copy the local configuration template:
   ```bash
   cp src/main/resources/application-local.yml.template src/main/resources/application-local.yml
   ```
3. Update `application-local.yml` with your PostgreSQL credentials
4. Build and run:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### API Documentation

Once running, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## Development

See [CLAUDE.md](CLAUDE.md) for comprehensive development guidelines.

## License

MIT
