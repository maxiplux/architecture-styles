# Shopping Cart REST API

**Version:** 2.0.0
**Project:** `app.quantun.architecture`
**Last Updated:** 2025-11-30
**Architecture:** Clean Architecture (Uncle Bob)

---

## Table of Contents

1. [Overview](#overview)
2. [Technical Stack](#technical-stack)
3. [Clean Architecture Overview](#clean-architecture-overview)
4. [Technical Decisions (TD)](#technical-decisions-td)
5. [Architecture](#architecture)
6. [Database Schema](#database-schema)
7. [API Endpoints](#api-endpoints)
8. [Sequence Diagrams](#sequence-diagrams)
9. [Getting Started](#getting-started)
10. [API Documentation](#api-documentation)
11. [Testing Strategy](#testing-strategy)

---

## Overview

The Shopping Cart REST API is a comprehensive backend solution for an e-commerce platform built with Spring Boot following **Clean Architecture** principles as defined by Robert C. Martin (Uncle Bob). This project demonstrates the separation of concerns through concentric circles, dependency inversion, and framework independence.

### Key Features

- **Clean Architecture Implementation** - Strict dependency rule with concentric circles
- **Domain-Driven Design** - Rich domain entities with business logic
- **Product Catalog Management** - Browse and search products with advanced filtering
- **Category Organization** - Hierarchical product categorization
- **Order Processing** - Complete order lifecycle management with inventory control
- **Use Case Driven** - Application logic encapsulated in interactors
- **Framework Independence** - Core business logic free from framework dependencies
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

// Database
runtimeOnly 'org.postgresql:postgresql'

// API Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14'

// Object Mapping (for Clean Architecture layer transformations)
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'

// Development Tools
compileOnly 'org.projectlombok:lombok'
developmentOnly 'org.springframework.boot:spring-boot-devtools'
developmentOnly 'org.springframework.boot:spring-boot-docker-compose'

// Testing
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'
```

---

## Clean Architecture Overview

This project implements Clean Architecture as defined by Robert C. Martin. The key principle is the **Dependency Rule**: source code dependencies must point inward, toward higher-level policies.

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

### Layer Descriptions

| Layer | Package | Description |
|-------|---------|-------------|
| **Entities** | `entity/` | Enterprise-wide business rules. Pure domain objects with business logic, NO framework dependencies |
| **Use Cases** | `usecase/` | Application-specific business rules. Interactors orchestrate data flow to/from entities |
| **Interface Adapters** | `interface_adapter/` | Controllers, Presenters, Gateway implementations. Convert data between use cases and external agencies |
| **Frameworks & Drivers** | `framework/` | JPA entities, repositories, Spring configuration. The outermost layer with all framework code |

### Core Principles

1. **Dependency Rule** - Dependencies only point inward. Inner layers know nothing about outer layers.
2. **Entities** - Enterprise-wide business rules with validation and domain logic.
3. **Use Cases** - Application-specific business rules via Interactors.
4. **Interface Adapters** - Convert data between use cases/entities and external agencies.
5. **Frameworks & Drivers** - The outermost layer containing frameworks and tools.
6. **Dependency Inversion** - High-level modules don't depend on low-level modules.

---

## Technical Decisions (TD)

### TD-001: Clean Architecture Pattern

**Decision:** Implement Clean Architecture with strict dependency rules and concentric circles.

**Rationale:**
- **Entities Layer** - Pure domain objects with business logic, no framework dependencies
- **Use Cases Layer** - Application business rules via Interactors with Input/Output boundaries
- **Interface Adapters Layer** - Controllers, Presenters, and Gateway implementations
- **Frameworks & Drivers Layer** - JPA entities, repositories, Spring configuration

**Benefits:**
- Framework independence - business logic is not tied to Spring
- Testability - inner layers can be tested without infrastructure
- Clear separation of concerns with explicit boundaries
- Flexibility to change frameworks without affecting business logic

### TD-002: JPA Specification Pattern for Dynamic Queries

**Decision:** Use `JpaSpecificationExecutor` with Specification pattern for dynamic product filtering.

**Rationale:**
- Avoids repository method explosion
- Type-safe query building with Criteria API
- Runtime query composition
- Reusable and combinable filter predicates

**Implementation:**
```java
// ProductRepository extends JpaSpecificationExecutor
Specification<Product> spec = Specification.allOf(
    ProductSpecifications.hasCategory(categoryId),
    ProductSpecifications.nameLike(name),
    ProductSpecifications.priceBetween(minPrice, maxPrice),
    ProductSpecifications.inStock(inStock)
);
Page<Product> results = productRepository.findAll(spec, pageable);
```

**Benefits:**
- No query method proliferation
- Flexible filter combinations
- Maintainable and extensible
- Supports complex AND/OR logic

### TD-003: DTO Pattern with Java Records

**Decision:** Use immutable DTOs (Data Transfer Objects) implemented as Java records for API contracts.

**Rationale:**
- Clear API contract separation from domain entities
- Immutability for thread safety
- Concise syntax with records
- Prevents accidental entity exposure

**Examples:**
```java
public record ProductDTO(Long id, String name, BigDecimal price, ...) {}
public record OrderResponse(Long orderId, String status, ...) {}
```

**Benefits:**
- API contract stability
- Reduced boilerplate code
- Prevention of lazy loading issues
- Security through data hiding

### TD-004: Flattened Shipping Address

**Decision:** Store shipping address as flat columns in `orders` table instead of separate entity.

**Rationale:**
- Simplifies data model for MVP scope
- Reduces join complexity
- Shipping addresses are order-specific (not reusable in this scope)
- Easier to query and report

**Trade-off:** Less normalized, but acceptable for current requirements.

### TD-005: BigDecimal for Monetary Values

**Decision:** Use `BigDecimal` for all monetary values (price, subtotal, tax, total).

**Rationale:**
- Precise decimal arithmetic (no floating-point errors)
- Industry standard for financial calculations
- Supports arbitrary precision

**Configuration:**
```java
@Column(nullable = false, precision = 12, scale = 2)
private BigDecimal price;
```

### TD-006: Transaction Boundaries on Interactors

**Decision:** Use `@Transactional` on Interactor methods (Use Case implementations).

**Rationale:**
- Transaction boundaries are defined at the use case level
- Interactors orchestrate the complete business operation
- Stock decrement after order creation within same transaction
- Rollback on any exception

**Implementation:**
```java
// usecase/order/CreateOrderInteractor.java
@Transactional
public OrderOutputData execute(CreateOrderInputData input) {
    // All operations within single transaction
}
```

**Future Enhancement:** Consider pessimistic locking or optimistic locking with `@Version` for high-concurrency scenarios.

### TD-007: Global Exception Handling

**Decision:** Centralized exception handling using `@RestControllerAdvice` in the Framework layer.

**Rationale:**
- Consistent error response format across all endpoints
- Separation of error handling from business logic
- Single source of truth for error responses

**Custom Exceptions (in `shared/exception/`):**
- `EntityNotFoundException` → 404
- `ValidationException` → 400
- `BusinessRuleException` → 409 (business rule violations)

### TD-008: Enum for Order Status

**Decision:** Use Java enum for order status instead of string constants.

**Rationale:**
- Type safety at compile time
- Prevents invalid status values
- Self-documenting code
- Easy to extend

**Status Flow:**
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
         ↘ CANCELLED
```

### TD-009: Soft Delete via Active Flag

**Decision:** Use `active` boolean flag for soft deletes instead of hard deletes.

**Rationale:**
- Preserve data for audit and reporting
- Enable product/category reactivation
- Simplify business logic (no cascading deletes)

### TD-010: API Versioning in URL Path

**Decision:** Include version prefix `/api/v1/` in all endpoint URLs.

**Rationale:**
- Clear API versioning strategy
- Enables backward compatibility
- Easy to introduce breaking changes in v2
- Industry best practice

### TD-011: Pagination by Default

**Decision:** All list endpoints return paginated results using Spring Data `Pageable`.

**Rationale:**
- Prevents performance issues with large datasets
- Supports sorting and filtering
- Standard Spring Data integration

### TD-012: Fixed Tax Rate

**Decision:** Use hardcoded 8% tax rate in `OrderService`.

**Rationale:**
- Simplifies MVP implementation
- Tax calculation logic is centralized
- Easy to refactor to configurable/regional rates later

```java
private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
```

### TD-013: Hibernate DDL Auto-Update

**Decision:** Use `spring.jpa.hibernate.ddl-auto=update` for development.

**Rationale:**
- Automatic schema synchronization during development
- Faster iteration cycle
- No manual migration scripts needed for MVP

**Production Consideration:** Switch to `validate` and use Flyway/Liquibase for production.

### TD-014: Docker Compose for Local Development

**Decision:** Use Docker Compose for PostgreSQL instead of embedded database.

**Rationale:**
- Production-like environment locally
- Consistent database across team
- Easy to reset and manage
- Spring Boot Docker Compose integration

### TD-015: Comprehensive OpenAPI Documentation

**Decision:** Extensive use of SpringDoc annotations for API documentation.

**Rationale:**
- Self-documenting API
- Interactive testing via Swagger UI
- Reduces need for separate documentation
- Contract-first development support

**Annotations Used:**
- `@Tag`, `@Operation`, `@Parameter`
- `@ApiResponse` with examples
- `@Schema` for DTO documentation

---

## Architecture

### Clean Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          FRAMEWORKS & DRIVERS                                    │
│                                                                                  │
│   framework/persistence/         framework/web/           framework/config/      │
│   - JPA Data Entities           - Exception Handler      - BeanConfiguration     │
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

### Request Flow Through Layers

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  Controller (Interface Adapter)                                  │
│  - Receives HTTP request                                         │
│  - Maps RequestModel → InputData                                 │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  Use Case Interactor                                             │
│  - Executes business logic                                       │
│  - Uses Gateways for data access                                 │
│  - Returns OutputData                                            │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  Gateway Implementation (Interface Adapter)                      │
│  - Implements Gateway interface                                  │
│  - Uses JPA Repository                                           │
│  - Maps DataEntity ↔ Domain Entity                               │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  JPA Repository (Framework)                                      │
│  - Executes database queries                                     │
│  - Returns JPA Data Entities                                     │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  Presenter (Interface Adapter)                                   │
│  - Maps OutputData → ResponseModel                               │
│  - Returns HTTP response                                         │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
HTTP Response
```

---

## Database Schema

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

### Table Details

#### categories
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique category identifier |
| name | VARCHAR(100) | NOT NULL | Category name |
| description | VARCHAR(500) | NULL | Category description |
| active | BOOLEAN | NOT NULL, DEFAULT true | Soft delete flag |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |

#### products
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique product identifier |
| name | VARCHAR(200) | NOT NULL | Product name |
| description | VARCHAR(1000) | NULL | Product description |
| price | DECIMAL(12,2) | NOT NULL | Product price |
| stock | INTEGER | NOT NULL | Available quantity |
| image_url | VARCHAR(500) | NULL | Product image URL |
| active | BOOLEAN | NOT NULL, DEFAULT true | Soft delete flag |
| category_id | BIGINT | FK → categories(id), NOT NULL | Parent category |

#### orders
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique order identifier |
| customer_id | BIGINT | NOT NULL | Customer identifier |
| status | VARCHAR(20) | NOT NULL | Order status enum |
| subtotal | DECIMAL(12,2) | NOT NULL | Order subtotal |
| tax | DECIMAL(12,2) | NOT NULL | Tax amount (8%) |
| total | DECIMAL(12,2) | NOT NULL | Total amount |
| created_at | TIMESTAMP | NOT NULL | Order creation time |
| shipping_street | VARCHAR(200) | NOT NULL | Shipping address street |
| shipping_city | VARCHAR(100) | NOT NULL | Shipping address city |
| shipping_state | VARCHAR(100) | NULL | Shipping address state |
| shipping_zip_code | VARCHAR(20) | NOT NULL | Shipping address zip |
| shipping_country | VARCHAR(100) | NOT NULL | Shipping address country |

#### order_items
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique order item identifier |
| order_id | BIGINT | FK → orders(id), NOT NULL | Parent order |
| product_id | BIGINT | FK → products(id), NOT NULL | Ordered product |
| quantity | INTEGER | NOT NULL | Quantity ordered |
| unit_price | DECIMAL(12,2) | NOT NULL | Price at order time |
| subtotal | DECIMAL(12,2) | NOT NULL | Line item subtotal |

### Relationships

- **Category ↔ Product**: One-to-Many (One category has many products)
- **CustomerOrder ↔ OrderItem**: One-to-Many (One order has many items)
- **Product ↔ OrderItem**: One-to-Many (One product can appear in many order items)

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

### 1. Get All Categories

**Endpoint:** `GET /api/v1/categories`

**Description:** Retrieves all active product categories.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "active": true,
    "createdAt": "2025-11-24T10:30:00Z"
  }
]
```

### 2. Search Products

**Endpoint:** `GET /api/v1/products`

**Description:** Search and filter products with pagination.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| categoryId | Long | No | Filter by category ID |
| name | String | No | Partial name match (case-insensitive) |
| minPrice | BigDecimal | No | Minimum price filter |
| maxPrice | BigDecimal | No | Maximum price filter |
| inStock | Boolean | No | Filter in-stock products |
| active | Boolean | No | Filter active products (default: true) |
| page | Integer | No | Page number (default: 0) |
| size | Integer | No | Page size (default: 20) |
| sort | String | No | Sort criteria (e.g., "price,asc") |

**Example Request:**
```
GET /api/v1/products?categoryId=1&maxPrice=500&inStock=true&sort=price,asc
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Wireless Headphones",
      "description": "Premium noise-canceling headphones",
      "price": 149.99,
      "categoryId": 1,
      "categoryName": "Electronics",
      "stock": 45,
      "imageUrl": "https://example.com/images/headphones.jpg",
      "active": true
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

### 3. Create Order

**Endpoint:** `POST /api/v1/orders`

**Description:** Create a new customer order.

**Request Body:**
```json
{
  "customerId": 42,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 5,
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

**Response:** `201 Created`
```json
{
  "orderId": 1001,
  "status": "PENDING",
  "items": [
    {
      "productId": 1,
      "productName": "Wireless Headphones",
      "quantity": 2,
      "unitPrice": 149.99,
      "subtotal": 299.98
    }
  ],
  "subtotal": 299.98,
  "tax": 24.00,
  "total": 323.98,
  "createdAt": "2025-11-24T16:30:00Z"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors, invalid product ID
- `404 Not Found` - Category not found
- `409 Conflict` - Insufficient stock, inactive product
- `500 Internal Server Error` - Server error

---

## Sequence Diagrams

### 1. Get Products with Filtering

```
Client          Controller       Service         Repository      Database
  │                 │               │                │              │
  │─GET /products──►│               │                │              │
  │  ?categoryId=1  │               │                │              │
  │                 │               │                │              │
  │                 │─search()─────►│                │              │
  │                 │  (filter)     │                │              │
  │                 │               │                │              │
  │                 │               │──validate─────►│              │
  │                 │               │  category      │              │
  │                 │               │                │──SELECT────►│
  │                 │               │                │◄─category───│
  │                 │               │◄───────────────│              │
  │                 │               │                │              │
  │                 │               │─build specs───►│              │
  │                 │               │  (Specification.allOf)        │
  │                 │               │                │              │
  │                 │               │─findAll()─────►│              │
  │                 │               │  (spec, page)  │              │
  │                 │               │                │──SELECT────►│
  │                 │               │                │  WHERE...   │
  │                 │               │                │◄─products───│
  │                 │               │◄───────────────│              │
  │                 │               │                │              │
  │                 │               │─map to DTO────►│              │
  │                 │◄──Page<DTO>───│                │              │
  │◄─200 OK────────│               │                │              │
  │  (products)     │               │                │              │
```

### 2. Create Order

```
Client        Controller      Service       Repository     Database
  │               │              │               │             │
  │─POST /orders─►│              │               │             │
  │  (request)    │              │               │             │
  │               │              │               │             │
  │               │─@Valid──────►│               │             │
  │               │              │               │             │
  │               │─createOrder()►│               │             │
  │               │  (request)   │               │             │
  │               │              │               │             │
  │               │              │───@Transactional────────────┤
  │               │              │               │             │
  │               │              │─validate items►│             │
  │               │              │               │             │
  │               │              │─findById()────►│             │
  │               │              │  (productIds) │──SELECT────►│
  │               │              │               │◄─products───│
  │               │              │◄──────────────│             │
  │               │              │               │             │
  │               │              │─check stock───│             │
  │               │              │  & active     │             │
  │               │              │               │             │
  │               │              │─calculate─────│             │
  │               │              │  totals       │             │
  │               │              │               │             │
  │               │              │─save(order)───►│             │
  │               │              │               │──INSERT────►│
  │               │              │               │  (orders)   │
  │               │              │               │──INSERT────►│
  │               │              │               │  (order_items)│
  │               │              │◄──────────────│             │
  │               │              │               │             │
  │               │              │─update stock──►│             │
  │               │              │               │──UPDATE────►│
  │               │              │               │  (products) │
  │               │              │◄──────────────│             │
  │               │              │               │             │
  │               │              │───commit transaction────────┤
  │               │              │               │             │
  │               │              │─map to DTO────│             │
  │               │◄─OrderResponse│               │             │
  │◄─201 Created──│              │               │             │
  │  (order)      │              │               │             │
```

### 3. Get All Categories

```
Client       Controller      Service       Repository    Database
  │              │              │               │            │
  │─GET /cats───►│              │               │            │
  │              │              │               │            │
  │              │─findAll()───►│               │            │
  │              │              │               │            │
  │              │              │─findByActive─►│            │
  │              │              │  True()       │            │
  │              │              │               │──SELECT───►│
  │              │              │               │  WHERE     │
  │              │              │               │  active=true│
  │              │              │               │◄─categories│
  │              │              │◄──────────────│            │
  │              │              │               │            │
  │              │              │─stream().map()│            │
  │              │              │  (to DTO)     │            │
  │              │◄─List<DTO>───│               │            │
  │◄─200 OK─────│              │               │            │
  │  (categories)│              │               │            │
```

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Gradle** (or use included wrapper)

### Installation & Running

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd architecture
```

#### 2. Start PostgreSQL with Docker Compose
```bash
docker-compose up -d
```

This will start PostgreSQL on `localhost:5432` with:
- Database: `mydatabase`
- Username: `myuser`
- Password: `secret`

#### 3. Run the Application
```bash
./gradlew bootRun
```

Or on Windows:
```bash
gradlew.bat bootRun
```

The application will start on `http://localhost:8080`.

#### 4. Initialize Sample Data

Sample data is automatically loaded on first startup via `DataInitializer`:
- 6 product categories
- 30+ computer store products

### Configuration

**Database Configuration** (`application.properties`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
spring.datasource.username=myuser
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update
```

**Actuator Endpoints:**
```properties
management.endpoints.web.exposure.include=health,info
```

Access health check: `http://localhost:8080/actuator/health`

---

## API Documentation

### Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification

- **JSON:** `http://localhost:8080/v3/api-docs`
- **YAML:** `http://localhost:8080/v3/api-docs.yaml`

### Testing with Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Explore available endpoints organized by tags (Categories, Products, Orders)
3. Click on any endpoint to expand details
4. Click "Try it out" to test endpoints interactively
5. Enter parameters and click "Execute"
6. View response body, status code, and headers

---

## Project Structure (Clean Architecture)

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── entity/                                   # ENTITIES (innermost)
│   ├── Category.java                        # Enterprise business rules
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   └── ShippingAddress.java                 # Value Object
│
├── usecase/                                  # USE CASES (Application Business Rules)
│   ├── category/
│   │   ├── GetAllCategoriesUseCase.java     # Input port (interface)
│   │   ├── GetAllCategoriesInteractor.java  # Implementation
│   │   └── CategoryOutputData.java          # Output DTO
│   ├── product/
│   │   ├── SearchProductsUseCase.java
│   │   ├── SearchProductsInteractor.java
│   │   ├── ProductSearchInputData.java      # Input DTO
│   │   └── ProductOutputData.java           # Output DTO
│   ├── order/
│   │   ├── CreateOrderUseCase.java
│   │   ├── CreateOrderInteractor.java
│   │   ├── CreateOrderInputData.java
│   │   ├── OrderItemInputData.java
│   │   └── OrderOutputData.java
│   └── gateway/                             # Data access interfaces (output boundaries)
│       ├── CategoryGateway.java
│       ├── ProductGateway.java
│       └── OrderGateway.java
│
├── interface_adapter/                        # INTERFACE ADAPTERS
│   ├── controller/                          # Controllers (input adapters)
│   │   ├── CategoryController.java
│   │   ├── ProductController.java
│   │   └── OrderController.java
│   ├── presenter/                           # Presenters (output adapters)
│   │   ├── CategoryPresenter.java
│   │   ├── ProductPresenter.java
│   │   └── OrderPresenter.java
│   ├── gateway/                             # Gateway implementations
│   │   ├── CategoryGatewayImpl.java
│   │   ├── ProductGatewayImpl.java
│   │   └── OrderGatewayImpl.java
│   └── dto/                                 # View Models for HTTP
│       ├── request/
│       │   ├── OrderCreateRequestModel.java
│       │   ├── OrderItemRequestModel.java
│       │   └── ShippingAddressRequestModel.java
│       └── response/
│           ├── CategoryResponseModel.java
│           ├── ProductResponseModel.java
│           ├── OrderResponseModel.java
│           ├── OrderItemResponseModel.java
│           └── PageResponseModel.java
│
├── framework/                                # FRAMEWORKS & DRIVERS (outermost)
│   ├── config/
│   │   ├── OpenApiConfig.java
│   │   ├── BeanConfiguration.java
│   │   └── DataInitializer.java
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── CategoryDataEntity.java      # JPA Entity
│   │   │   ├── ProductDataEntity.java
│   │   │   ├── OrderDataEntity.java
│   │   │   └── OrderItemDataEntity.java
│   │   ├── repository/
│   │   │   ├── CategoryJpaRepository.java
│   │   │   ├── ProductJpaRepository.java
│   │   │   ├── OrderJpaRepository.java
│   │   │   └── OrderItemJpaRepository.java
│   │   ├── mapper/
│   │   │   ├── CategoryDataMapper.java
│   │   │   ├── ProductDataMapper.java
│   │   │   └── OrderDataMapper.java
│   │   └── specification/
│   │       └── ProductDataSpecifications.java
│   └── web/
│       └── exception/
│           └── GlobalExceptionHandler.java
│
└── shared/                                   # Cross-cutting utilities
    └── exception/
        ├── EntityNotFoundException.java
        ├── BusinessRuleException.java
        └── ValidationException.java
```

### Test Structure (Mirrors Clean Architecture)

```
src/test/java/app/quantun/architecture/
├── entity/                          # Innermost - fastest tests
│   ├── CategoryTest.java
│   ├── ProductTest.java
│   └── OrderTest.java
├── usecase/                         # Application business rules
│   ├── category/
│   │   └── GetAllCategoriesInteractorTest.java
│   ├── product/
│   │   └── SearchProductsInteractorTest.java
│   └── order/
│       └── CreateOrderInteractorTest.java
├── interface_adapter/               # Interface adapters
│   ├── controller/
│   │   ├── CategoryControllerTest.java
│   │   ├── ProductControllerTest.java
│   │   └── OrderControllerTest.java
│   └── gateway/
│       ├── CategoryGatewayImplTest.java
│       ├── ProductGatewayImplTest.java
│       └── OrderGatewayImplTest.java
├── framework/                       # Outermost - infrastructure tests
│   └── persistence/
│       └── mapper/
│           ├── ProductDataMapperTest.java
│           └── OrderDataMapperTest.java
├── integration/                     # Full stack tests
│   ├── CreateOrderFullStackTest.java
│   └── ProductSearchFullStackTest.java
└── architecture/                    # Architecture compliance tests
    └── CleanArchitectureTest.java
```

---

## Testing Strategy

Clean Architecture provides exceptional testability because of its strict separation of concerns and dependency rules.

### Test Layers

| Layer | Test Type | Speed | Dependencies |
|-------|-----------|-------|--------------|
| **Entities** | Unit Tests | Fastest | None (pure Java) |
| **Use Cases** | Unit Tests | Fast | Mocked Gateways |
| **Interface Adapters** | Integration Tests | Medium | Spring Context |
| **Frameworks** | Integration Tests | Slow | Database, External Services |

### Architecture Tests with ArchUnit

```java
@Test
void entitiesShouldNotDependOnUseCases() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat()
        .resideInAPackage("..usecase..");
    
    rule.check(classes);
}

@Test
void useCasesShouldNotDependOnFrameworks() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..usecase..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..framework..", "org.springframework..");
    
    rule.check(classes);
}
```

---

## Development Notes

### Clean Architecture Best Practices

1. **Dependency Rule** - Dependencies only point inward toward higher-level policies
2. **Entities Are NOT JPA Entities** - Separate domain entities from persistence entities
3. **Use Cases Return Output Data** - Not domain entities directly
4. **Controllers Use Presenters** - For transforming output data to response models
5. **Gateways Are Interfaces** - Defined in use case layer, implemented in interface adapter layer
6. **Interactors Have No Annotations** - Pure Java classes, no Spring annotations
7. **BeanConfiguration Wires Use Cases** - Manual bean creation in config class
8. **Preserve Validation** - In request DTOs (framework level) and entities (domain level)
9. **Transaction Boundaries** - On interactor methods, using @Transactional
10. **MapStruct for Mapping** - Type-safe transformations between layers

### SOLID Principles Applied

- **Single Responsibility** - Each class has one reason to change
- **Open/Closed** - Open for extension, closed for modification
- **Liskov Substitution** - Interfaces define contracts
- **Interface Segregation** - Small, focused interfaces (Use Cases, Gateways)
- **Dependency Inversion** - High-level modules don't depend on low-level modules

### Future Enhancements

- Add authentication/authorization (Spring Security + JWT)
- Implement caching (Redis)
- Implement optimistic locking with `@Version`
- Add event-driven architecture (Spring Events/Kafka)
- Implement database migrations (Flyway/Liquibase)
- Add monitoring and metrics (Prometheus/Grafana)
- Implement rate limiting
- Add search with Elasticsearch
- Support multiple payment methods

---

## License

Apache 2.0

---

## Contact

- **API Support:** support@quantun.app
- **Website:** https://quantun.app

---

**Last Updated:** 2025-11-30
