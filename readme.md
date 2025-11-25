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

The Shopping Cart REST API is a comprehensive backend solution for an e-commerce platform built with Spring Boot. This project demonstrates clean architecture principles, modern Java patterns, and best practices for building scalable REST APIs.

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

// Development Tools
compileOnly 'org.projectlombok:lombok'
developmentOnly 'org.springframework.boot:spring-boot-devtools'
developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
```

---

## Technical Decisions (TD)

### TD-001: Hexagonal Architecture (Ports & Adapters)

**Decision:** Implement Hexagonal Architecture (Ports and Adapters pattern) with clear separation between domain, application, and infrastructure concerns.

**Rationale:**
- **Domain Layer** - Pure business logic with zero framework dependencies
- **Application Layer** - Use case definitions (ports) and implementations (services)
  - **Driving Ports (Inbound)** - Use case interfaces defining what the application can do
  - **Driven Ports (Outbound)** - Repository interfaces defining what the application needs
- **Adapter Layer** - Implementations connecting ports to external systems
  - **Driving Adapters** - Web controllers, CLI commands (future)
  - **Driven Adapters** - JPA persistence, external APIs (future)

**Benefits:**
- Domain logic testable without infrastructure dependencies
- Easy to swap databases, web frameworks, or add new adapters
- Clear boundaries through explicit port interfaces
- Framework independence in core business logic
- Changes in one adapter don't affect others

**Package Structure:**
```
application/
├── port/in/          # Use case interfaces
├── port/out/         # Repository interfaces
└── service/          # Use case implementations

domain/
├── model/            # Pure domain entities (no JPA)
└── exception/        # Domain exceptions

adapter/
├── in/web/           # Controllers, DTOs, Mappers
└── out/persistence/  # JPA entities, repositories, adapters
```

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
    ProductJpaSpecifications.hasCategory(categoryId),
    ProductJpaSpecifications.nameLike(name),
    ProductJpaSpecifications.priceBetween(minPrice, maxPrice),
    ProductJpaSpecifications.inStock(inStock)
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
public record ProductResponse(Long id, String name, BigDecimal price, ...) {}
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

### TD-006: Optimistic Locking Strategy

**Decision:** Use transaction boundaries without explicit pessimistic locks for order creation.

**Rationale:**
- `@Transactional` ensures ACID properties
- Stock decrement after order creation within same transaction
- Rollback on any exception
- Acceptable for initial implementation

**Future Enhancement:** Consider pessimistic locking or optimistic locking with `@Version` for high-concurrency scenarios.

### TD-007: Global Exception Handling

**Decision:** Centralized exception handling using `@RestControllerAdvice`.

**Rationale:**
- Consistent error response format across all endpoints
- Separation of error handling from business logic
- Single source of truth for error responses

**Custom Exceptions:**
- `NotFoundException` → 404
- `BadRequestException` → 400
- `ConflictException` → 409 (business rule violations)

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

### Hexagonal Architecture Overview

This project implements **Hexagonal Architecture** (Ports and Adapters pattern), which isolates business logic from external concerns through well-defined boundaries.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│         (Web Browser, Mobile App, Postman, Swagger UI)          │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DRIVING ADAPTERS                            │
│                     (adapter/in/web/)                            │
│                                                                   │
│  Web Controllers (REST endpoints, DTOs, Exception Handlers)      │
│  • CategoryController  • ProductController  • OrderController    │
└────────────────────────────┬────────────────────────────────────┘
                             │ uses
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                            │
│                                                                   │
│  ┌──────────────────────┐       ┌──────────────────────────┐    │
│  │  Driving Ports (In)  │       │   Service Layer          │    │
│  │  (Use Cases)         │◄──────│   (Implementations)      │    │
│  │                      │       │                          │    │
│  │ GetCategoriesUseCase │       │  CategoryServiceImpl     │    │
│  │ SearchProductsUseCase│       │  ProductServiceImpl      │    │
│  │ CreateOrderUseCase   │       │  OrderServiceImpl        │    │
│  └──────────────────────┘       └──────────┬───────────────┘    │
│                                            │ uses                │
│                                            ▼                     │
│                          ┌───────────────────────────────┐      │
│                          │   Driven Ports (Out)          │      │
│                          │   (Repository Interfaces)     │      │
│                          │                               │      │
│                          │  CategoryRepositoryPort       │      │
│                          │  ProductRepositoryPort        │      │
│                          │  OrderRepositoryPort          │      │
│                          └─────────┬─────────────────────┘      │
└────────────────────────────────────┼────────────────────────────┘
                                     │ implemented by
┌────────────────────────────────────▼────────────────────────────┐
│                      DRIVEN ADAPTERS                             │
│                  (adapter/out/persistence/)                      │
│                                                                   │
│  Persistence Layer (JPA Entities, Repositories, Mappers)         │
│  • CategoryJpaEntity, CategoryJpaRepository                      │
│  • ProductJpaEntity, ProductJpaRepository                        │
│  • OrderJpaEntity, OrderJpaRepository                            │
│  • Persistence Mappers (Domain ↔ JPA Entity)                    │
│  • JPA Specifications (Dynamic Queries)                          │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC
                             ▼
                    ┌────────────────────┐
                    │   PostgreSQL DB    │
                    │  (Categories,      │
                    │   Products,        │
                    │   Orders,          │
                    │   OrderItems)      │
                    └────────────────────┘

        ┌────────────────────────────────────────────┐
        │            DOMAIN LAYER                    │
        │         (domain/model/)                    │
        │                                            │
        │  Pure Business Logic - No Framework Deps   │
        │  • Category, Product, Order, OrderItem     │
        │  • OrderStatus, ShippingAddress            │
        │  • Domain Exceptions                       │
        │                                            │
        │  Used by Application Layer                 │
        └────────────────────────────────────────────┘
```

### Hexagonal Architecture Flow

```
HTTP Request
    ↓
Controller (Driving Adapter)
    ↓ calls
Use Case Port (Driving Port - interface)
    ↓ implemented by
Service (Application Layer)
    ↓ uses Domain Models & calls
Repository Port (Driven Port - interface)
    ↓ implemented by
Repository Adapter (Driven Adapter)
    ↓ uses
JPA Entity & JPA Repository
    ↓
Database
```

### Key Architecture Components

| Layer | Package | Responsibility | Dependencies |
|-------|---------|----------------|--------------|
| **Domain** | `domain/model/` | Pure business logic, entities, rules | None (framework-independent) |
| **Application** | `application/port/` | Use case interfaces (ports) | Domain only |
| **Application** | `application/service/` | Use case implementations | Domain, ports |
| **Driving Adapter** | `adapter/in/web/` | REST controllers, DTOs | Application ports |
| **Driven Adapter** | `adapter/out/persistence/` | JPA entities, repositories | Application ports, Domain |
| **Config** | `config/` | Spring configuration, data initialization | All layers |

### Architecture Benefits

1. **Testability** - Domain and application logic testable without infrastructure
2. **Flexibility** - Easy to swap databases or web frameworks
3. **Maintainability** - Clear boundaries, changes isolated to specific adapters
4. **Framework Independence** - Core business logic has zero Spring/JPA dependencies
5. **Extensibility** - Easy to add new adapters (CLI, messaging, GraphQL, etc.)

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

## Project Structure

This project follows **Hexagonal Architecture** package organization:

```
architecture/
├── src/
│   ├── main/
│   │   ├── java/app/quantun/architecture/
│   │   │   │
│   │   │   ├── application/                    # APPLICATION LAYER
│   │   │   │   ├── port/
│   │   │   │   │   ├── in/                    # Driving Ports (Use Cases)
│   │   │   │   │   │   ├── GetCategoriesUseCase.java
│   │   │   │   │   │   ├── SearchProductsUseCase.java
│   │   │   │   │   │   ├── CreateOrderUseCase.java
│   │   │   │   │   │   ├── CreateOrderCommand.java
│   │   │   │   │   │   ├── OrderItemCommand.java
│   │   │   │   │   │   └── ProductSearchCriteria.java
│   │   │   │   │   └── out/                   # Driven Ports (Repositories)
│   │   │   │   │       ├── CategoryRepositoryPort.java
│   │   │   │   │       ├── ProductRepositoryPort.java
│   │   │   │   │       └── OrderRepositoryPort.java
│   │   │   │   └── service/                   # Use Case Implementations
│   │   │   │       ├── CategoryServiceImpl.java
│   │   │   │       ├── ProductServiceImpl.java
│   │   │   │       └── OrderServiceImpl.java
│   │   │   │
│   │   │   ├── domain/                         # DOMAIN LAYER
│   │   │   │   ├── model/                     # Pure Domain Entities
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── Product.java
│   │   │   │   │   ├── Order.java
│   │   │   │   │   ├── OrderItem.java
│   │   │   │   │   ├── OrderStatus.java
│   │   │   │   │   └── ShippingAddress.java
│   │   │   │   └── exception/                 # Domain Exceptions
│   │   │   │       ├── DomainException.java
│   │   │   │       ├── CategoryNotFoundException.java
│   │   │   │       ├── ProductNotFoundException.java
│   │   │   │       ├── InsufficientStockException.java
│   │   │   │       └── ProductNotActiveException.java
│   │   │   │
│   │   │   ├── adapter/                        # ADAPTERS LAYER
│   │   │   │   ├── in/web/                    # Driving Adapters (Controllers)
│   │   │   │   │   ├── CategoryController.java
│   │   │   │   │   ├── ProductController.java
│   │   │   │   │   ├── OrderController.java
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── dto/                   # Data Transfer Objects
│   │   │   │   │   │   ├── CategoryResponse.java
│   │   │   │   │   │   ├── ProductResponse.java
│   │   │   │   │   │   ├── ProductFilterRequest.java
│   │   │   │   │   │   ├── OrderCreateRequest.java
│   │   │   │   │   │   ├── OrderResponse.java
│   │   │   │   │   │   ├── OrderItemRequest.java
│   │   │   │   │   │   ├── OrderItemResponse.java
│   │   │   │   │   │   └── ShippingAddressRequest.java
│   │   │   │   │   └── mapper/                # Web Mappers (DTO ↔ Domain)
│   │   │   │   │       ├── CategoryWebMapper.java
│   │   │   │   │       ├── ProductWebMapper.java
│   │   │   │   │       └── OrderWebMapper.java
│   │   │   │   └── out/persistence/           # Driven Adapters
│   │   │   │       ├── entity/                # JPA Entities
│   │   │   │       │   ├── CategoryJpaEntity.java
│   │   │   │       │   ├── ProductJpaEntity.java
│   │   │   │       │   ├── OrderJpaEntity.java
│   │   │   │       │   └── OrderItemJpaEntity.java
│   │   │   │       ├── repository/            # Spring Data JPA Repositories
│   │   │   │       │   ├── CategoryJpaRepository.java
│   │   │   │       │   ├── ProductJpaRepository.java
│   │   │   │       │   ├── OrderJpaRepository.java
│   │   │   │       │   └── OrderItemJpaRepository.java
│   │   │   │       ├── adapter/               # Adapter Implementations
│   │   │   │       │   ├── CategoryRepositoryAdapter.java
│   │   │   │       │   ├── ProductRepositoryAdapter.java
│   │   │   │       │   └── OrderRepositoryAdapter.java
│   │   │   │       ├── mapper/                # Persistence Mappers (Domain ↔ JPA Entity)
│   │   │   │       │   ├── CategoryPersistenceMapper.java
│   │   │   │       │   ├── ProductPersistenceMapper.java
│   │   │   │       │   ├── OrderPersistenceMapper.java
│   │   │   │       │   └── OrderItemPersistenceMapper.java
│   │   │   │       └── specification/         # JPA Specifications (Dynamic Queries)
│   │   │   │           └── ProductJpaSpecifications.java
│   │   │   │
│   │   │   ├── config/                        # Configuration
│   │   │   │   ├── DataInitializer.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   │
│   │   │   └── ArchitectureApplication.java   # Spring Boot Main Class
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/app/quantun/architecture/
│           └── ArchitectureApplicationTests.java
│
├── docs/
│   ├── Shopping_Cart_API_PRD.md              # Product Requirements Document
│   └── 02-HEXAGONAL-ARCHITECTURE.md          # Detailed Architecture Guide
│
├── build.gradle                               # Gradle Build Configuration
├── compose.yaml                               # Docker Compose (PostgreSQL)
└── README.md                                  # This File
```

### Package Organization by Layer

| Package | Layer | Description |
|---------|-------|-------------|
| `application/port/in/` | Application | Use case interfaces (driving ports) |
| `application/port/out/` | Application | Repository interfaces (driven ports) |
| `application/service/` | Application | Use case implementations |
| `domain/model/` | Domain | Pure business entities (no JPA) |
| `domain/exception/` | Domain | Domain-specific exceptions |
| `adapter/out/persistence/` | Infrastructure | JPA entities, repositories, adapters |
| `adapter/in/web/` | Infrastructure | REST controllers (driving adapter) |
| `adapter/in/web/dto/` | Infrastructure | API request/response objects |
| `config/` | Infrastructure | Spring configuration |

---

## Development Notes

### Best Practices Implemented

1. **Clean Architecture** - Clear separation of concerns across layers
2. **SOLID Principles** - Single responsibility, dependency injection
3. **DRY Principle** - Specification pattern eliminates query duplication
4. **Immutable DTOs** - Using Java records for thread-safe data transfer
5. **Validation** - Jakarta Validation annotations at DTO level
6. **Exception Handling** - Centralized, consistent error responses
7. **Transaction Management** - Proper `@Transactional` boundaries
8. **API Documentation** - Comprehensive OpenAPI annotations

### Future Enhancements

- Add authentication/authorization (Spring Security + JWT)
- Implement caching (Redis)
- Add comprehensive unit and integration tests
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

**Last Updated:** 2025-11-24
