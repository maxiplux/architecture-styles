# Product Requirements Document
## Shopping Cart REST API

**Project:** `app.quantun.architecture`
**Version:** 2.0.0
**Date:** 2025-11-30
**Status:** Active
**Architecture:** Clean Architecture (Uncle Bob)

---

## 1. Executive Summary

This document outlines the requirements for a Shopping Cart REST API built using Spring Boot following **Clean Architecture** principles as defined by Robert C. Martin (Uncle Bob). The API provides core e-commerce functionality including product catalog management, category organization, and order processing. This project demonstrates the separation of concerns through concentric circles, dependency inversion, and framework independence.

---

## 2. Project Overview

### 2.1 Purpose

The Shopping Cart API serves as a backend foundation for an e-commerce platform, enabling client applications to interact with the product catalog and manage customer orders through a standardized REST interface.

### 2.2 Scope

The initial release focuses on three core use cases:

1. **Get All Categories** - Retrieve the complete list of product categories
2. **Get Products by Category** - Fetch products with optional category filtering
3. **Create Order** - Process and persist customer orders

### 2.3 Out of Scope

- Authentication and authorization
- Payment processing
- Shipping calculations
- User management

---

## 3. Technical Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.5.8 |
| Language | Java 21 |
| Database | PostgreSQL (latest via Docker) |
| ORM | Spring Data JPA with JpaSpecificationExecutor |
| Dynamic Queries | JPA Criteria API via Specification pattern |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Validation | Spring Boot Starter Validation |
| Build Tool | Gradle |
| Utilities | Lombok |
| Object Mapping | MapStruct 1.5.5 |
| Containerization | Docker Compose |
| Architecture Testing | ArchUnit 1.2.1 |

### 3.1 Key Dependencies

```groovy
// Data & Persistence
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

// API Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui'

// Validation
implementation 'org.springframework.boot:spring-boot-starter-validation'

// Object Mapping (for Clean Architecture layer transformations)
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'

// Architecture Testing
testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'
```

### 3.2 Clean Architecture Layers

The application follows Clean Architecture with four concentric circles:

| Layer | Package | Description |
|-------|---------|-------------|
| **Entities** | `entity/` | Enterprise-wide business rules. Pure domain objects with business logic, NO framework dependencies |
| **Use Cases** | `usecase/` | Application-specific business rules. Interactors orchestrate data flow to/from entities |
| **Interface Adapters** | `interface_adapter/` | Controllers, Presenters, Gateway implementations. Convert data between use cases and external agencies |
| **Frameworks & Drivers** | `framework/` | JPA entities, repositories, Spring configuration. The outermost layer with all framework code |

### 3.3 JpaSpecificationExecutor

JPA Repositories in the Framework layer extend `JpaSpecificationExecutor<T>` to support dynamic query building using the Specification pattern. This enables:

- Complex filtering without multiple repository methods
- Runtime query composition
- Type-safe criteria building
- Reusable filter predicates

### 3.4 Docker Compose Configuration

```yaml
services:
  postgres:
    image: 'postgres:latest'
    environment:
      - 'POSTGRES_DB=mydatabase'
      - 'POSTGRES_PASSWORD=secret'
      - 'POSTGRES_USER=myuser'
    ports:
      - '5432'
```

---

## 4. Use Cases

### 4.1 UC-001: Get All Categories

| Attribute | Description |
|-----------|-------------|
| **Use Case ID** | UC-001 |
| **Name** | Get All Categories |
| **Actor** | Client Application |
| **Preconditions** | System is operational; database contains category data |
| **Description** | Retrieve a complete list of all available product categories |
| **Postconditions** | A list of categories is returned to the client |
| **Priority** | High |

#### Basic Flow

1. Client sends GET request to `/api/v1/categories`
2. System retrieves all categories from the database
3. System returns 200 OK with JSON array of category objects

#### API Specification

| Method | Endpoint | Parameters | Responses |
|--------|----------|------------|-----------|
| GET | `/api/v1/categories` | None | 200: Category array, 500: Server error |

#### Response Schema

```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "active": true,
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

---

### 4.2 UC-002: Get Products by Category

| Attribute | Description |
|-----------|-------------|
| **Use Case ID** | UC-002 |
| **Name** | Get Products with Dynamic Filtering |
| **Actor** | Client Application |
| **Preconditions** | System is operational; products exist in the database |
| **Description** | Retrieve a list of products with dynamic filtering using JpaSpecificationExecutor |
| **Postconditions** | A filtered or complete list of products is returned |
| **Priority** | High |

#### Basic Flow

1. Client sends GET request to `/api/v1/products` with optional filter parameters
2. System builds dynamic query using JPA Specifications
3. System retrieves products from database applying all filters
4. System returns 200 OK with paginated JSON array of product objects

#### Alternative Flows

- **Invalid Category:** If `categoryId` does not exist, return 404 Not Found
- **Empty Results:** If no products match criteria, return 200 with empty array

#### API Specification

| Method | Endpoint | Parameters | Responses |
|--------|----------|------------|-----------|
| GET | `/api/v1/products` | `categoryId` (optional): Long | 200: Product array |
| | | `name` (optional): String (partial match) | 404: Category not found |
| | | `minPrice` (optional): BigDecimal | 500: Server error |
| | | `maxPrice` (optional): BigDecimal | |
| | | `inStock` (optional): Boolean | |
| | | `active` (optional): Boolean, default true | |
| | | `page` (optional): Integer, default 0 | |
| | | `size` (optional): Integer, default 20 | |
| | | `sort` (optional): String, e.g., "price,desc" | |

#### Dynamic Query Implementation

Using `JpaSpecificationExecutor`, filters are combined at runtime:

```java
public interface ProductRepository extends 
    JpaRepository<Product, Long>, 
    JpaSpecificationExecutor<Product> {
}

// Specification example
public class ProductSpecifications {
    
    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> 
            categoryId == null ? null : cb.equal(root.get("categoryId"), categoryId);
    }
    
    public static Specification<Product> nameLike(String name) {
        return (root, query, cb) -> 
            name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
    
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }
    
    public static Specification<Product> inStock(Boolean inStock) {
        return (root, query, cb) -> 
            inStock == null || !inStock ? null : cb.greaterThan(root.get("stock"), 0);
    }
}
```

#### Request Examples

```bash
# Get all active products
GET /api/v1/products

# Filter by category
GET /api/v1/products?categoryId=1

# Complex filter: Electronics under $200, in stock, sorted by price
GET /api/v1/products?categoryId=1&maxPrice=200&inStock=true&sort=price,asc

# Search by name with pagination
GET /api/v1/products?name=wireless&page=0&size=10
```

#### Response Schema

```json
{
  "content": [
    {
      "id": 101,
      "name": "Wireless Headphones",
      "description": "Premium noise-canceling headphones",
      "price": 149.99,
      "categoryId": 1,
      "categoryName": "Electronics",
      "stock": 50,
      "imageUrl": "https://cdn.example.com/products/101.jpg",
      "active": true
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

---

### 4.3 UC-003: Create Order

| Attribute | Description |
|-----------|-------------|
| **Use Case ID** | UC-003 |
| **Name** | Create Order |
| **Actor** | Client Application |
| **Preconditions** | Cart contains valid products with sufficient stock |
| **Description** | Submit an order containing selected products to complete a purchase |
| **Postconditions** | Order is created, inventory is updated, confirmation returned |
| **Priority** | Critical |

#### Basic Flow

1. Client sends POST request to `/api/v1/orders` with order payload
2. System validates all product IDs and quantities
3. System verifies stock availability for each item
4. System calculates order total
5. System creates order record and order items in database (transactional)
6. System decrements product stock quantities
7. System returns 201 Created with order confirmation

#### Alternative Flows

- **Insufficient Stock:** Return 409 Conflict with details of unavailable items
- **Invalid Product:** Return 400 Bad Request if product ID does not exist
- **Validation Error:** Return 400 Bad Request with validation details

#### API Specification

| Method | Endpoint | Request Body | Responses |
|--------|----------|--------------|-----------|
| POST | `/api/v1/orders` | Order JSON | 201: Order created |
| | | | 400: Invalid request |
| | | | 409: Stock conflict |
| | | | 500: Server error |

#### Request Schema

```json
{
  "customerId": 5001,
  "items": [
    { "productId": 101, "quantity": 2 },
    { "productId": 205, "quantity": 1 }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "Springfield",
    "state": "IL",
    "zipCode": "62701",
    "country": "USA"
  }
}
```

#### Response Schema

```json
{
  "orderId": 78001,
  "status": "PENDING",
  "items": [
    {
      "productId": 101,
      "productName": "Wireless Headphones",
      "quantity": 2,
      "unitPrice": 149.99,
      "subtotal": 299.98
    }
  ],
  "subtotal": 449.97,
  "tax": 36.00,
  "total": 485.97,
  "createdAt": "2024-01-20T14:30:00Z"
}
```

---

## 5. Data Model

### 5.1 Clean Architecture Entity Separation

In Clean Architecture, we distinguish between **Domain Entities** (innermost layer) and **Data Entities** (framework layer):

| Type | Location | Purpose |
|------|----------|---------|
| **Domain Entities** | `entity/` | Pure business logic, no framework dependencies |
| **Data Entities** | `framework/persistence/entity/` | JPA annotations, database mapping |

### 5.2 Domain Entity Relationship Diagram

```
┌──────────────────┐       ┌──────────────────┐
│  Category        │       │  Product         │
│  (Domain Entity) │       │  (Domain Entity) │
├──────────────────┤       ├──────────────────┤
│ id               │───┐   │ id               │
│ name             │   │   │ name             │
│ description      │   └──►│ category         │
│ active           │       │ price            │
│ createdAt        │       │ stock            │
│                  │       │ description      │
│ + isActive()     │       │ imageUrl         │
│                  │       │ active           │
└──────────────────┘       │                  │
                           │ + isAvailable()  │
                           │ + canFulfill()   │
                           │ + reduceStock()  │
                           └──────────────────┘
                                  │
                                  │
┌──────────────────┐       ┌──────────────────┐
│  Order           │       │  OrderItem       │
│  (Domain Entity) │       │  (Domain Entity) │
├──────────────────┤       ├──────────────────┤
│ id               │───┐   │ id               │
│ customerId       │   │   │ productId        │
│ status           │   └──►│ productName      │
│ items            │       │ quantity         │
│ subtotal         │       │ unitPrice        │
│ tax              │       │ subtotal         │
│ total            │       │                  │
│ createdAt        │       │ + getSubtotal()  │
│ shippingAddress  │       └──────────────────┘
│                  │
│ + createNew()    │       ┌──────────────────┐
│ + confirm()      │       │ ShippingAddress  │
│ + cancel()       │       │ (Value Object)   │
└──────────────────┘       ├──────────────────┤
                           │ street           │
                           │ city             │
                           │ state            │
                           │ zipCode          │
                           │ country          │
                           └──────────────────┘
```

### 5.3 Entity Definitions

| Entity | Layer | Key Fields | Description |
|--------|-------|------------|-------------|
| Category | Domain | id, name, description, active | Product categories with business logic |
| Product | Domain | id, name, price, category, stock | Items with availability and stock logic |
| Order | Domain | id, customerId, status, items, total | Order with calculation and state logic |
| OrderItem | Domain | productId, quantity, unitPrice | Line items with subtotal calculation |
| ShippingAddress | Domain | street, city, state, zipCode, country | Value Object for address |

### 5.4 Order Status Enum

| Status | Description |
|--------|-------------|
| PENDING | Order created, awaiting processing |
| CONFIRMED | Order confirmed and being prepared |
| SHIPPED | Order has been shipped |
| DELIVERED | Order delivered to customer |
| CANCELLED | Order was cancelled |

### 5.5 Use Case Input/Output Data

For Clean Architecture, use cases define their own input and output data structures:

```java
// Input Data (Use Case Layer)
public record ProductSearchInputData(
    Long categoryId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean inStock,
    Boolean active,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {}

// Output Data (Use Case Layer)
public record ProductOutputData(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Long categoryId,
    String categoryName,
    Integer stock,
    String imageUrl,
    boolean active
) {}
```

---

## 6. Non-Functional Requirements

### 6.1 Performance

- API response time < 200ms for 95th percentile under normal load
- Database queries optimized with appropriate indexing
- Pagination required for list endpoints

### 6.2 Reliability

- Transactional integrity for order creation (all-or-nothing)
- Transaction boundaries defined at Use Case (Interactor) level
- Proper error handling with meaningful error messages
- Graceful degradation under high load

### 6.3 Maintainability

- Clean Architecture with strict dependency rules
- Entities layer has NO framework dependencies
- Use Cases layer has NO Spring annotations (except @Transactional)
- Comprehensive logging using SLF4J
- API versioning in URL path (`/api/v1/`)

### 6.4 Testability

- Entity layer tests require NO mocking (pure unit tests)
- Use Case tests mock only Gateway interfaces
- Interface Adapter tests use Spring MockMvc
- Architecture tests with ArchUnit enforce dependency rules

### 6.5 Observability

- Spring Boot Actuator endpoints enabled for health checks
- Structured logging for debugging

---

## 7. Validation Rules

### 7.1 Order Validation

| Field | Rules |
|-------|-------|
| customerId | Required, positive integer |
| items | Required, non-empty array |
| items[].productId | Required, must exist in database |
| items[].quantity | Required, minimum 1, maximum 99 |
| shippingAddress.street | Required, max 200 characters |
| shippingAddress.city | Required, max 100 characters |
| shippingAddress.zipCode | Required, valid format |

---

## 8. Acceptance Criteria

| Use Case | Criteria | Status |
|----------|----------|--------|
| UC-001 | Returns all active categories with correct schema | Pending |
| UC-001 | Response time < 100ms for up to 100 categories | Pending |
| UC-002 | Correctly filters products by categoryId | Pending |
| UC-002 | Correctly filters products by name (partial match) | Pending |
| UC-002 | Correctly filters products by price range | Pending |
| UC-002 | Correctly filters products by stock availability | Pending |
| UC-002 | Combines multiple filters using AND logic | Pending |
| UC-002 | Pagination works with page, size, and sort parameters | Pending |
| UC-002 | Returns 404 for non-existent category | Pending |
| UC-003 | Creates order with valid request body | Pending |
| UC-003 | Stock is decremented after successful order | Pending |
| UC-003 | Returns 409 when stock is insufficient | Pending |
| UC-003 | Rollback on partial failure (transactional) | Pending |
| OpenAPI | Swagger UI accessible at /swagger-ui.html | Pending |
| OpenAPI | All endpoints documented with descriptions | Pending |

---

## 9. API Documentation (OpenAPI)

### 9.1 Swagger UI Access

| Resource | URL |
|----------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

### 9.2 OpenAPI Configuration

Located in `framework/config/`:

```java
// framework/config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Shopping Cart API")
                .version("2.0.0")
                .description("REST API for shopping cart operations - Clean Architecture"));
    }
}
```

### 9.3 Controller Annotations

Controllers in the Interface Adapter layer use SpringDoc annotations:

```java
// interface_adapter/controller/ProductController.java
@Tag(name = "Products", description = "Product catalog operations")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final SearchProductsUseCase searchProductsUseCase;
    private final ProductPresenter presenter;

    @Operation(summary = "Get products with dynamic filtering")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping
    public PageResponseModel<ProductResponseModel> getProducts(
        @Parameter(description = "Filter by category ID")
        @RequestParam(required = false) Long categoryId,
        @Parameter(description = "Filter by product name (partial match)")
        @RequestParam(required = false) String name,
        @ParameterObject Pageable pageable) {
        
        // Map to Use Case Input Data
        ProductSearchInputData inputData = new ProductSearchInputData(...);
        
        // Execute Use Case
        PagedProductOutputData outputData = searchProductsUseCase.execute(inputData);
        
        // Present Result
        return presenter.present(outputData);
    }
}
```

---

## 10. API Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/categories` | List all categories |
| GET | `/api/v1/products` | List products with dynamic filtering |
| POST | `/api/v1/orders` | Create a new order |
| - | `/swagger-ui.html` | Interactive API documentation |
| - | `/v3/api-docs` | OpenAPI specification (JSON) |

---

## 11. HTTP Status Codes

| Code | Status | Usage |
|------|--------|-------|
| 200 | OK | Successful GET requests |
| 201 | Created | Successful POST creating new resource |
| 400 | Bad Request | Invalid request body or parameters |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Business rule violation (e.g., insufficient stock) |
| 500 | Internal Error | Unexpected server error |

---

## 12. Clean Architecture Guidelines

### 12.1 Dependency Rule

Dependencies must only point inward. Inner layers know nothing about outer layers:

```
Entities ← Use Cases ← Interface Adapters ← Frameworks & Drivers
```

### 12.2 Layer Responsibilities

| Layer | Responsibilities | Allowed Dependencies |
|-------|------------------|---------------------|
| **Entities** | Business rules, validation | None (pure Java) |
| **Use Cases** | Application logic, orchestration | Entities only |
| **Interface Adapters** | Data conversion, HTTP handling | Use Cases, Entities |
| **Frameworks** | Database, Spring, external libs | All inner layers |

### 12.3 Key Implementation Rules

1. **Entities Are NOT JPA Entities** - Separate domain entities from persistence entities
2. **Use Cases Return Output Data** - Not domain entities directly
3. **Controllers Use Presenters** - For transforming output data to response models
4. **Gateways Are Interfaces** - Defined in use case layer, implemented in interface adapter layer
5. **Interactors Have No Annotations** - Pure Java classes, no Spring annotations (except @Transactional)
6. **BeanConfiguration Wires Use Cases** - Manual bean creation in config class

---

## 13. Assumptions & Constraints

### Assumptions

- Customer IDs are provided by the client (no user management in scope)
- Product images are hosted externally (URL reference only)
- Tax calculation uses a fixed rate (8%) in Order entity

### Constraints

- No authentication/authorization layer
- Single-region deployment
- No caching layer in initial implementation
- Strict adherence to Clean Architecture dependency rules

---

## Appendix: Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2024-11-24 | API Dev Team | Initial draft |
| 2.0 | 2025-11-30 | API Dev Team | Updated for Clean Architecture |
