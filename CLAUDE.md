# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Start PostgreSQL (required)
docker-compose up -d

# Build and run
./gradlew bootRun              # Run application (Windows: gradlew.bat bootRun)
./gradlew build                # Build project
./gradlew test                 # Run all tests
./gradlew test --tests "ProductTest"  # Run specific test class
./gradlew test --tests "CreateOrderInteractorTest.execute_withValidInput_createsOrder"  # Run single test
```

**Endpoints:**
- API Base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

## Clean Architecture Structure

This is a Spring Boot 3.5.8 / Java 21 shopping cart API following **Clean Architecture** principles.

### Package Structure (Concentric Circles)

```
src/main/java/app/quantun/architecture/
├── entity/                    # ENTITIES (innermost) - Pure domain objects
├── usecase/                   # USE CASES - Application business rules
│   ├── category/             # Category use cases
│   ├── product/              # Product use cases
│   ├── order/                # Order use cases
│   └── gateway/              # Gateway interfaces (output boundaries)
├── presentation/              # PRESENTATION ADAPTER
│   ├── rest/
│   │   ├── controller/       # REST controllers
│   │   ├── dto/
│   │   │   ├── request/      # Request models
│   │   │   └── response/     # Response models
│   │   └── exception/        # Global exception handler
│   └── presenter/            # Output formatters
├── persistence/               # PERSISTENCE ADAPTER
│   ├── entity/               # JPA entities (JpaEntity suffix)
│   ├── repository/           # Spring Data repositories
│   ├── gateway/              # Gateway implementations
│   ├── mapper/               # Domain <-> JPA entity mappers
│   └── specification/        # JPA Specifications
├── config/                    # CONFIGURATION
│   ├── BeanConfiguration     # Use case bean wiring
│   ├── OpenApiConfig         # Swagger/OpenAPI
│   └── DataInitializer       # Sample data initialization
└── shared/                    # Cross-cutting concerns
    └── exception/            # Domain exceptions
```

### Dependency Rule

Dependencies only point inward. Inner layers know nothing about outer layers:
- `entity/` → No dependencies on other layers
- `usecase/` → Depends only on `entity/` and `shared/`
- `presentation/` → Depends on `usecase/`, `entity/` (not on `persistence/`)
- `persistence/` → Depends on `usecase/`, `entity/` (not on `presentation/`)
- `config/` → Can depend on all layers (wiring)

## Key Patterns

**Pure Domain Entities** (`entity/`): Business logic with factory methods and validation. No framework annotations.

**Use Case Interactors** (`usecase/`): Pure Java classes implementing use case interfaces. Wired in `BeanConfiguration`.

**Gateways** (`usecase/gateway/`): Interfaces defined in use case layer, implemented in `persistence/gateway/`.

**Presenters** (`presentation/presenter/`): Transform use case output data to HTTP response models.

**JPA Specifications** (`persistence/specification/`): Dynamic query building for filtering.

**Manual Data Mappers** (`persistence/mapper/`): Transform between domain entities and JPA entities.

**Global Exception Handling**: `EntityNotFoundException`, `BusinessRuleException`, `ValidationException` → HTTP responses.

## Database

PostgreSQL via Docker Compose. Tables: `categories`, `products`, `orders`, `order_items`.

All monetary values use `BigDecimal` with `DECIMAL(12,2)`. Tax rate is 8% (in `Order` entity).

Sample data auto-loads via `DataInitializer` on startup.

## Testing

Tests organized by architectural layer:
- `entity/` - Pure unit tests, no mocking needed
- `usecase/` - Mock gateways with Mockito
- `archtest/` - ArchUnit tests enforcing Clean Architecture rules
- Integration tests use H2 in-memory database

Run with `./gradlew test`.

## Technical Decisions Reference

Detailed technical decisions documented in `docs/readme.md` and Clean Architecture guidelines in `docs/03-CLEAN-ARCHITECTURE.md`.
