# Shopping Cart REST API

**Version:** 1.0.0  
**Project:** `app.quantun.architecture`  
**Last Updated:** 2025-11-24

---

## Table of Contents

1. [Overview](#overview)
2. [Technical Stack](#technical-stack)
3. [Technical Decisions (TD)](#technical-decisions-td)
4. [Architecture](#architecture)
5. [Database Schema](#database-schema)
6. [API Endpoints](#api-endpoints)
7. [Sequence Diagrams](#sequence-diagrams)
8. [Getting Started](#getting-started)
9. [API Documentation](#api-documentation)

---

## Overview

The Shopping Cart REST API is a comprehensive backend solution for an e-commerce platform built with Spring Boot. This project demonstrates **Clean Architecture** principles (as defined by Robert C. Martin), modern Java patterns, and best practices for building scalable REST APIs.

### Key Features

- **Product Catalog Management** - Browse and search products with advanced filtering
- **Category Organization** - Hierarchical product categorization
- **Order Processing** - Complete order lifecycle management with inventory control
- **Dynamic Query Builder** - JPA Specification pattern for flexible filtering
- **OpenAPI Documentation** - Interactive Swagger UI for API exploration
- **Containerized Infrastructure** - Docker Compose for PostgreSQL database

### Out of Scope

- Authentication and authorization
- Payment processing
- Shipping calculations
- User management

---

## Technical Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.5.8 |
| **Language** | Java | 21 |
| **Database** | PostgreSQL | latest |
| **ORM** | Spring Data JPA + Hibernate | - |
| **Mapping** | MapStruct | 1.5.5 |
| **Build Tool** | Gradle | - |
| **API Documentation** | SpringDoc OpenAPI | 2.8.14 |
| **Validation** | Jakarta Validation | - |
| **Utilities** | Lombok | - |
| **Containerization** | Docker Compose | - |

### Key Dependencies

```gradle
// Core Spring Boot Starters
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-actuator'

// Mapping
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'

// Database
runtimeOnly 'org.postgresql:postgresql'

// API Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14'

// Development Tools
compileOnly 'org.projectlombok:lombok'
developmentOnly 'org.springframework.boot:spring-boot-devtools'
developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
```

---

## Technical Decisions (TD)

### TD-001: Clean Architecture

**Decision:** Implement strict Clean Architecture with concentric circles and dependency rules.

**Rationale:**
- **Entities Layer** - Innermost layer, enterprise business rules, no framework dependencies.
- **Use Cases Layer** - Application business rules, orchestrates data flow.
- **Interface Adapters** - Controllers, Presenters, Gateways (convert data for external agencies).
- **Frameworks & Drivers** - Outermost layer (Database, Web Framework, Config).

**Benefits:**
- **Dependency Rule:** Source code dependencies only point inward.
- **Independence:** The business logic is independent of UI, Database, and Frameworks.
- **Testability:** Business rules can be tested without external elements.

### TD-002: MapStruct for Data Transformation

**Decision:** Use MapStruct for mapping between layers (Response Models ↔ Output Data ↔ Domain Entities ↔ Data Entities).

**Rationale:**
- Maintains the strict separation of layers by converting objects at boundaries.
- compile-time safety and high performance.
- Eliminates boilerplate mapping code.

### TD-003: JPA Specification Pattern for Dynamic Queries

**Decision:** Use `JpaSpecificationExecutor` in the Gateway implementation (Infrastructure layer) for dynamic filtering.

**Rationale:**
- Allows the Use Case layer to request filtered data via abstract criteria.
- Keeps SQL/JPA logic contained in the outermost layer.
- Type-safe query building.

### TD-004: Explicit Use Case Interfaces

**Decision:** Each action (e.g., `CreateOrder`, `GetProducts`) has its own Use Case interface and implementation (Interactor).

**Rationale:**
- Adheres to Single Responsibility Principle (SRP).
- clearly defines the Input/Output ports of the application.
- avoids "God classes" (like massive Service classes).

### TD-005: Presenter Pattern

**Decision:** Use Presenters to format Output Data from Use Cases into Response Models for the View/Controller.

**Rationale:**
- Decouples the Use Case from the format of the HTTP response.
- Allows for different presentation logic for different delivery mechanisms (Web, CLI, etc.).

### TD-006: Domain Entities vs Persistence Entities

**Decision:** Distinct classes for Domain Entities (Business Rules) and Persistence Entities (JPA).

**Rationale:**
- **Domain Entities:** Rich models with behavior, no annotations.
- **Persistence Entities:** Anemic models with `@Entity`, `@Table` annotations, optimized for DB storage.
- Prevents database schema changes from leaking into business logic.

### TD-007: Global Exception Handling

**Decision:** Centralized exception handling using `@RestControllerAdvice`.

**Rationale:**
- Consistent error response format.
- Maps domain exceptions (e.g., `EntityNotFoundException`, `BusinessRuleException`) to HTTP status codes.

### TD-008: Enum for Order Status

**Decision:** Use Java enum for order status.

**Status Flow:**
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
         ↘ CANCELLED
```

### TD-009: Soft Delete via Active Flag

**Decision:** Use `active` boolean flag for soft deletes.

### TD-010: API Versioning in URL Path

**Decision:** Include version prefix `/api/v1/`.

### TD-011: Pagination by Default

**Decision:** List endpoints return paginated results.

### TD-012: Comprehensive OpenAPI Documentation

**Decision:** Extensive use of SpringDoc annotations on Controllers.

---

## Architecture

### The Concentric Circles

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│    FRAMEWORKS & DRIVERS (outermost)                                         │
│    ┌─────────────────────────────────────────────────────────────────────┐  │
│    │                                                                     │  │
│    │   INTERFACE ADAPTERS                                                │  │
│    │   ┌─────────────────────────────────────────────────────────────┐   │  │
│    │   │                                                             │   │  │
│    │   │   USE CASES (Application Business Rules)                    │   │  │
│    │   │   ┌─────────────────────────────────────────────────────┐   │   │  │
│    │   │   │                                                     │   │   │  │
│    │   │   │   ENTITIES (Enterprise Business Rules)              │   │   │  │
│    │   │   │                                                     │   │   │  │
│    │   │   └─────────────────────────────────────────────────────┘   │   │  │
│    │   │                                                             │   │  │
│    │   └─────────────────────────────────────────────────────────────┘   │  │
│    │                                                                     │  │
│    └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          FRAMEWORKS & DRIVERS                                    │
│                                                                                  │
│   framework/persistence/         framework/web/           framework/config/      │
│   - JPA Entities                - Exception Handler      - BeanConfiguration     │
│   - JPA Repositories            - OpenApiConfig          - DataInitializer       │
│   - Data Mappers                                                                 │
│   - Specifications                                                               │
│                                                                                  │
└───────────────────────────────────────┬─────────────────────────────────────────┘
                                        │ implements
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          INTERFACE ADAPTERS                                      │
│                                                                                  │
│   Controllers               Presenters               Gateway Impls               │
│   - CategoryController      - CategoryPresenter      - CategoryGatewayImpl       │
│   - ProductController       - ProductPresenter       - ProductGatewayImpl        │
│   - OrderController         - OrderPresenter         - OrderGatewayImpl          │
│                                                                                  │
│   DTOs (Request/Response Models)                                                 │
│   - CategoryResponseModel                                                        │
│   - OrderCreateRequestModel                                                      │
│                                                                                  │
└───────────────────────────────────────┬─────────────────────────────────────────┘
                                        │ uses
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              USE CASES                                           │
│                                                                                  │
│   Input Boundaries (Interfaces)        Output Boundaries (Gateways)              │
│   - GetAllCategoriesUseCase           - CategoryGateway                          │
│   - SearchProductsUseCase             - ProductGateway                           │
│   - CreateOrderUseCase                - OrderGateway                             │
│                                                                                  │
│   Interactors (Implementations)        Input/Output Data                         │
│   - GetAllCategoriesInteractor        - ProductSearchInputData                   │
│   - SearchProductsInteractor          - CategoryOutputData                       │
│   - CreateOrderInteractor             - OrderOutputData                          │
│                                                                                  │
└───────────────────────────────────────┬─────────────────────────────────────────┘
                                        │ uses
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ENTITIES                                            │
│                                                                                  │
│   Category        Product        Order        OrderItem        ShippingAddress   │
│   (domain)        (domain)       (domain)     (domain)         (value object)    │
│                                                                                  │
│   - Business logic methods                                                       │
│   - Validation rules                                                             │
│   - No framework dependencies                                                    │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Database Schema

(Schema remains the same as previous versions, managed by JPA Entities in the Framework layer)

### Entity Relationship Diagram (ERD)

```
┌──────────────────────┐
│      Category        │
├──────────────────────┤
│ id (PK)        BIGINT│
│ name          VARCHAR│
│ description   VARCHAR│
│ active        BOOLEAN│
│ created_at  TIMESTAMP│
└───────────┬──────────┘
            │
            │ 1
            │
            │ N
            ▼
┌──────────────────────┐
│       Product        │
├──────────────────────┤
│ id (PK)        BIGINT│
│ name          VARCHAR│
│ description   VARCHAR│
│ price      DECIMAL(12,2)│
│ stock            INT  │
│ image_url     VARCHAR│
│ active        BOOLEAN│
│ category_id (FK) BIGINT│
└───────────┬──────────┘
            │
            │
            │ N (referenced by)
            │
┌───────────▼──────────┐       ┌──────────────────────┐
│     OrderItem        │   N:1 │   CustomerOrder      │
├──────────────────────┤◄──────├──────────────────────┤
│ id (PK)        BIGINT│       │ id (PK)        BIGINT│
│ order_id (FK)  BIGINT│       │ customer_id    BIGINT│
│ product_id (FK) BIGINT│      │ status        VARCHAR│
│ quantity          INT │       │ subtotal   DECIMAL(12,2)│
│ unit_price DECIMAL(12,2)│    │ tax        DECIMAL(12,2)│
│ subtotal   DECIMAL(12,2)│    │ total      DECIMAL(12,2)│
└──────────────────────┘       │ created_at  TIMESTAMP│
                               │ shipping_street VARCHAR│
                               │ shipping_city   VARCHAR│
                               │ shipping_state  VARCHAR│
                               │ shipping_zip_code VARCHAR│
                               │ shipping_country VARCHAR│
                               └──────────────────────┘
```

---

## API Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/categories` | List all active categories |
| GET | `/products` | Search products with filters |
| POST | `/orders` | Create a new order |

(Detailed endpoint documentation matches the PRD)

---

## Sequence Diagrams

### 1. Get Products with Filtering (Clean Architecture Flow)

```
Client      Controller      Interactor      Gateway       Repository      Database
  │             │               │              │              │              │
  │─GET────────►│               │              │              │              │
  │             │               │              │              │              │
  │             │─execute()────►│              │              │              │
  │             │ (InputData)   │              │              │              │
  │             │               │─findAll()───►│              │              │
  │             │               │ (Criteria)   │              │              │
  │             │               │              │─findAll()───►│              │
  │             │               │              │ (Spec)       │──SELECT────►│
  │             │               │              │              │◄─Entities───│
  │             │               │              │◄─Domain Obj──│              │
  │             │               │◄─PagedResult─│              │              │
  │             │               │              │              │              │
  │             │◄─OutputData───│              │              │              │
  │             │               │              │              │              │
  │─Present────►│               │              │              │              │
  │             │               │              │              │              │
  ◄─Response────│               │              │              │              │
```

### 2. Create Order (Clean Architecture Flow)

```
Client      Controller      Interactor      Gateway       Repository      Database
  │             │               │              │              │              │
  │─POST───────►│               │              │              │              │
  │             │               │              │              │              │
  │             │─execute()────►│              │              │              │
  │             │ (InputData)   │              │              │              │
  │             │               │─findAll()───►│              │              │
  │             │               │              │─findByIds()─►│              │
  │             │               │◄─Products────│              │              │
  │             │               │              │              │              │
  │             │─Business Logic│              │              │              │
  │             │ (Validation)  │              │              │              │
  │             │               │              │              │              │
  │             │               │─save()──────►│              │              │
  │             │               │              │─save()──────►│──INSERT────►│
  │             │               │◄─SavedOrder──│              │              │
  │             │               │              │              │              │
  │             │◄─OutputData───│              │              │              │
  │             │               │              │              │              │
  │─Present────►│               │              │              │              │
  ◄─Response────│               │              │              │              │
```

---

## Project Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── entity/                                   # ENTITIES (innermost)
│   ├── Category.java
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── ...
│
├── usecase/                                  # USE CASES (Application Business Rules)
│   ├── category/
│   │   ├── GetAllCategoriesUseCase.java
│   │   ├── GetAllCategoriesInteractor.java
│   │   └── CategoryOutputData.java
│   ├── product/
│   │   ├── SearchProductsUseCase.java
│   │   ├── SearchProductsInteractor.java
│   │   └── ...
│   ├── order/
│   │   ├── CreateOrderUseCase.java
│   │   ├── CreateOrderInteractor.java
│   │   └── ...
│   └── gateway/                             # Data access interfaces
│       └── ...
│
├── interface_adapter/                        # INTERFACE ADAPTERS
│   ├── controller/                          # Controllers
│   ├── presenter/                           # Presenters
│   ├── gateway/                             # Gateway Implementations
│   └── dto/                                 # View Models
│
├── framework/                                # FRAMEWORKS & DRIVERS (outermost)
│   ├── config/
│   ├── persistence/
│   │   ├── entity/                          # JPA Entities
│   │   ├── repository/
│   │   └── mapper/
│   └── web/
│       └── exception/
│
└── shared/                                   # Cross-cutting utilities
    └── exception/
```

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Gradle**

### Installation & Running

1. **Clone the Repository**
2. **Start Database**: `docker-compose up -d`
3. **Run Application**: `./gradlew bootRun`

The application will start on `http://localhost:8080`.

---

## License

Apache 2.0

---

## Contact

- **API Support:** support@quantun.app
- **Website:** https://quantun.app

---

**Last Updated:** 2025-11-24
