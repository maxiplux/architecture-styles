# Layered Architecture (N-Tier) - Current Implementation

## Overview

This document describes the **current architecture** of the Shopping Cart API project. It serves as the baseline reference for understanding the existing structure before refactoring to other architectural styles.

Layered Architecture (also known as N-Tier Architecture) is the most common architectural pattern in enterprise applications. It organizes code into horizontal layers, where each layer has a specific responsibility and only communicates with adjacent layers.

---

## Core Principles

1. **Separation of Concerns**: Each layer handles a specific aspect of the application (presentation, business logic, data access).

2. **Dependency Direction**: Upper layers depend on lower layers. Controllers depend on Services, Services depend on Repositories.

3. **Layer Isolation**: Each layer should be independent enough to be modified without significantly impacting other layers.

4. **Single Responsibility per Layer**: Presentation layer handles HTTP, Service layer handles business logic, Repository layer handles data access.

---

## Current Package Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java      # Spring Boot entry point
├── config/                           # Configuration classes
│   ├── DataInitializer.java         # Sample data seeding
│   └── OpenApiConfig.java           # Swagger/OpenAPI configuration
├── domain/                           # Entity layer (JPA entities)
│   ├── Category.java
│   ├── Product.java
│   ├── CustomerOrder.java
│   ├── OrderItem.java
│   └── OrderStatus.java             # Enum
├── dto/                              # Data Transfer Objects
│   ├── CategoryDTO.java             # Record
│   ├── ProductDTO.java              # Record
│   ├── ProductFilter.java           # Filter criteria class
│   └── order/
│       ├── OrderCreateRequest.java
│       ├── OrderItemRequest.java
│       ├── OrderItemResponse.java   # Record
│       ├── OrderResponse.java       # Record
│       └── ShippingAddressDTO.java
├── exception/                        # Exception handling
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── BadRequestException.java
│   ├── ConflictException.java
│   └── NotFoundException.java
├── repository/                       # Data Access layer
│   ├── CategoryRepository.java
│   ├── ProductRepository.java       # Extends JpaSpecificationExecutor
│   ├── CustomerOrderRepository.java
│   └── OrderItemRepository.java
├── service/                          # Business Logic layer
│   ├── CategoryService.java
│   ├── ProductService.java
│   └── OrderService.java
├── spec/                             # JPA Specifications
│   └── ProductSpecifications.java   # Dynamic query building
└── web/                              # Presentation layer (REST)
    ├── CategoryController.java
    ├── ProductController.java
    └── OrderController.java
```

---

## Layer Responsibilities

### Presentation Layer (`web/`)

Handles HTTP request/response processing. Controllers receive requests, delegate to services, and return responses.

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

**Responsibilities:**
- HTTP request mapping and routing
- Request validation (`@Valid`)
- Response status codes
- OpenAPI documentation annotations
- Delegating to service layer

### Service Layer (`service/`)

Contains business logic and orchestrates operations. Services are transactional boundaries.

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // Business logic: validate products, check stock, calculate totals
        // Persist order and update inventory
        // Return response DTO
    }
}
```

**Responsibilities:**
- Business logic implementation
- Transaction management (`@Transactional`)
- Coordination between repositories
- DTO ↔ Entity mapping
- Business validation rules

### Repository Layer (`repository/`)

Data access abstraction using Spring Data JPA.

```java
public interface ProductRepository extends 
    JpaRepository<Product, Long>, 
    JpaSpecificationExecutor<Product> {
}

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrue();
}
```

**Responsibilities:**
- CRUD operations
- Custom queries
- Dynamic queries via Specifications
- Database interaction abstraction

### Domain Layer (`domain/`)

JPA entities representing database tables.

```java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    // ... other fields
}
```

**Responsibilities:**
- Database table mapping
- JPA annotations and relationships
- Basic validation constraints
- Lifecycle callbacks (`@PrePersist`)

### DTO Layer (`dto/`)

Data Transfer Objects for API contracts, separate from entities.

```java
public record ProductDTO(
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

**Responsibilities:**
- API request/response contracts
- Decoupling API from domain model
- Validation annotations for requests
- Immutability (using records)

### Exception Handling (`exception/`)

Centralized error handling with `@RestControllerAdvice`.

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }
    
    // ... other handlers
}
```

### Specifications (`spec/`)

Dynamic query building using JPA Criteria API.

```java
public final class ProductSpecifications {
    
    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> 
            categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }
    
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }
}
```

---

## Data Flow

```
HTTP Request
     │
     ▼
┌─────────────────┐
│   Controller    │  ← Validates request, extracts parameters
│   (web/)        │
└────────┬────────┘
         │ DTO
         ▼
┌─────────────────┐
│    Service      │  ← Business logic, transactions, DTO mapping
│   (service/)    │
└────────┬────────┘
         │ Entity
         ▼
┌─────────────────┐
│   Repository    │  ← Data access, queries
│  (repository/)  │
└────────┬────────┘
         │ SQL
         ▼
┌─────────────────┐
│    Database     │
│   (PostgreSQL)  │
└─────────────────┘
```

---

## Strengths of Current Architecture

1. **Simplicity**: Easy to understand and follow the code flow.
2. **Familiarity**: Most Java developers know this pattern well.
3. **Spring Boot Alignment**: Matches Spring Boot's default conventions.
4. **Quick Development**: Fast to implement CRUD operations.
5. **Tooling Support**: IDE navigation works well with clear layer boundaries.

---

## Limitations of Current Architecture

1. **Domain Logic Leakage**: Business rules can spread across layers.
2. **Tight Coupling**: Services directly depend on JPA repositories and entities.
3. **Testing Challenges**: Hard to test business logic without database.
4. **Framework Lock-in**: Domain entities are tied to JPA annotations.
5. **Scalability**: Difficult to split into microservices later.
6. **Cross-cutting Concerns**: Features like auditing span multiple layers.

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `ArchitectureApplication.java` | Spring Boot main class |
| `OpenApiConfig.java` | Swagger UI configuration |
| `DataInitializer.java` | Sample data seeding on startup |
| `GlobalExceptionHandler.java` | Centralized error handling |
| `ProductSpecifications.java` | Dynamic query building |
| `OrderService.java` | Most complex business logic |

---

## API Endpoints

| Method | Endpoint | Controller | Service Method |
|--------|----------|------------|----------------|
| GET | `/api/v1/categories` | `CategoryController.getAll()` | `CategoryService.findAll()` |
| GET | `/api/v1/products` | `ProductController.getProducts()` | `ProductService.search()` |
| POST | `/api/v1/orders` | `OrderController.create()` | `OrderService.createOrder()` |

---

## Dependencies Between Layers

```
web/ ──────────► service/ ──────────► repository/
  │                 │                     │
  │                 │                     │
  ▼                 ▼                     ▼
dto/              domain/              domain/
                    │
                    ▼
                  spec/
```

---

## Configuration Files

| File | Purpose |
|------|---------|
| `application.properties` | Database, JPA, Actuator, OpenAPI settings |
| `compose.yaml` | Docker Compose for PostgreSQL |
| `build.gradle` | Dependencies and build configuration |

---

## Notes for AI Agent

When using this as a baseline for refactoring:

1. **Preserve Functionality**: All three use cases must work after refactoring:
   - GET `/api/v1/categories` - List all active categories
   - GET `/api/v1/products` - Search products with filters
   - POST `/api/v1/orders` - Create order with stock validation

2. **Preserve API Contracts**: Request/response schemas must remain identical.

3. **Preserve Database Schema**: Entity mappings should produce the same tables.

4. **Preserve OpenAPI Documentation**: Swagger UI should still work.

5. **Preserve Sample Data**: `DataInitializer` logic should be maintained.

6. **Preserve Exception Handling**: Same HTTP status codes for errors.

This document serves as the "before" state for all architectural refactoring exercises.
