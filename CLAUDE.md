# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.8 e-commerce REST API application called "Shopping Cart REST API" built with Java 21. The project demonstrates clean architecture principles with a layered approach for product catalog management, category organization, and order processing.

## Development Commands

### Build and Run
```bash
# Run the application (starts on port 8080)
./gradlew bootRun

# On Windows
gradlew.bat bootRun

# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Build Docker image
./gradlew bootBuildImage
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with verbose output
./gradlew test --info
```

### Database
```bash
# Start PostgreSQL database
docker-compose up -d

# Stop database
docker-compose down
```

## Architecture Overview

### Package Structure
- `domain/` - JPA entities (Product, Category, CustomerOrder, OrderItem)
- `web/` - REST controllers with OpenAPI documentation
- `service/` - Business logic layer with transaction management
- `repository/` - JPA repositories with Specification pattern
- `dto/` - Data Transfer Objects (using Java records)
- `exception/` - Custom exceptions and global exception handler
- `spec/` - JPA Specification classes for dynamic queries
- `config/` - Configuration classes and data initialization

### Key Technical Patterns

#### JPA Specification Pattern
The project uses JPA Specifications for dynamic query building, particularly in `ProductRepository` which extends `JpaSpecificationExecutor<Product>`. This pattern is implemented in `ProductSpecifications` class for composable, type-safe query building.

#### Layered Architecture
- Controllers handle HTTP requests/responses and delegate to services
- Services contain business logic and transaction boundaries
- Repositories handle data access using Spring Data JPA
- DTOs separate API contracts from domain entities

#### Transaction Management
Services use `@Transactional` annotations for transaction boundaries, particularly important in `OrderService` for order creation with stock management.

## Database Configuration

Uses PostgreSQL with Docker Compose. Connection details in `application.properties`:
- URL: `jdbc:postgresql://localhost:5432/mydatabase`
- Username: `myuser`
- Password: `secret`

Sample data is automatically initialized via `DataInitializer` on first startup.

## API Documentation

- Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## Development Notes

- Java 21 language features are used
- Lombok annotations reduce boilerplate (ensure annotation processing is enabled)
- Jakarta Validation for request validation
- BigDecimal for all monetary calculations
- Enum `OrderStatus` for order state management
- Soft deletes using `active` boolean flags
- Fixed 8% tax rate in OrderService

## Common Development Tasks

When adding new entities:
1. Create domain entity in `domain/` package
2. Create repository extending `JpaRepository` in `repository/` package
3. Create service class with business logic in `service/` package
4. Create DTOs for API contracts in `dto/` package
5. Create controller with OpenAPI annotations in `web/` package

When adding complex queries:
- Use Specification pattern in `spec/` package
- Repository should extend `JpaSpecificationExecutor`
- Build composable specifications for reusable query logic