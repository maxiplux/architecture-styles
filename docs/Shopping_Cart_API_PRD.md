# Product Requirements Document
## Shopping Cart REST API

**Project:** `app.quantun.architecture`  
**Version:** 1.0.0
**Date:** 2025-11-24
**Status:** Draft

---

## 1. Executive Summary

This document outlines the requirements for a Shopping Cart REST API built using Spring Boot. The project explicitly follows **Clean Architecture** principles to demonstrate how to decouple business rules from frameworks, databases, and external interfaces.

---

## 2. Project Overview

### 2.1 Purpose

The Shopping Cart API serves as a backend foundation for an e-commerce platform. It is designed to be highly testable, maintainable, and independent of external frameworks.

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
| Architecture | Clean Architecture (Robert C. Martin) |
| Framework | Spring Boot 3.5.8 |
| Language | Java 21 |
| Database | PostgreSQL (latest via Docker) |
| ORM | Spring Data JPA |
| Mapping | MapStruct 1.5.5 |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Validation | Spring Boot Starter Validation |
| Build Tool | Gradle |
| Containerization | Docker Compose |

### 3.1 MapStruct Integration

To maintain strict separation between layers (e.g., Domain Entities vs. Persistence Entities vs. DTOs), MapStruct is used for efficient, type-safe object mapping.

### 3.2 Docker Compose Configuration

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
| **Input Port** | `GetAllCategoriesUseCase` |
| **Interactor** | `GetAllCategoriesInteractor` |
| **Output Port** | `CategoryGateway` |
| **Description** | Retrieve a complete list of all available product categories |
| **Priority** | High |

#### Basic Flow

1. **Controller** receives GET request.
2. Controller calls `GetAllCategoriesUseCase.execute()`.
3. **Interactor** calls `CategoryGateway.findAllActive()`.
4. **Gateway Implementation** (Interface Adapter) retrieves data from DB and maps to Domain Entities.
5. Interactor returns list of `CategoryOutputData`.
6. **Presenter** formats output into `CategoryResponseModel`.
7. Controller returns 200 OK with JSON.

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
| **Input Port** | `SearchProductsUseCase` |
| **Interactor** | `SearchProductsInteractor` |
| **Output Port** | `ProductGateway` |
| **Description** | Retrieve a list of products with dynamic filtering |
| **Priority** | High |

#### Basic Flow

1. **Controller** maps request parameters to `ProductSearchInputData`.
2. Controller calls `SearchProductsUseCase.execute(input)`.
3. **Interactor** calls `ProductGateway.findAll(input)`.
4. **Gateway Implementation** builds dynamic query (using JPA Specifications) and returns `PagedResult<Product>`.
5. Interactor maps Domain Entities to `ProductOutputData`.
6. **Presenter** maps to `PageResponseModel`.
7. Controller returns 200 OK.

#### API Specification

| Method | Endpoint | Parameters | Responses |
|--------|----------|------------|-----------|
| GET | `/api/v1/products` | `categoryId`, `name`, `minPrice`, `maxPrice`, `inStock`, `page`, `size` | 200: Product array |

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
  "size": 20
}
```

---

### 4.3 UC-003: Create Order

| Attribute | Description |
|-----------|-------------|
| **Use Case ID** | UC-003 |
| **Name** | Create Order |
| **Input Port** | `CreateOrderUseCase` |
| **Interactor** | `CreateOrderInteractor` |
| **Output Port** | `OrderGateway`, `ProductGateway` |
| **Description** | Submit an order containing selected products to complete a purchase |
| **Priority** | Critical |

#### Basic Flow

1. **Controller** maps request body to `CreateOrderInputData`.
2. Controller calls `CreateOrderUseCase.execute(input)`.
3. **Interactor** validates input and retrieves products via `ProductGateway`.
4. **Interactor** executes business logic (Entity method `Product.canFulfillQuantity()`).
5. **Interactor** creates `Order` domain entity.
6. **Interactor** persists order via `OrderGateway` and updates stock.
7. **Presenter** returns `OrderResponseModel`.

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

## 5. Data Model (Domain Entities)

In Clean Architecture, these are the **Innermost Circle**. They have **NO** dependencies on frameworks or annotations.

```
┌──────────────┐       ┌──────────────┐
│   Category   │       │   Product    │
├──────────────┤       ├──────────────┤
│ id           │───┐   │ id           │
│ name         │   │   │ name         │
│ description  │   └──►│ category     │
│ active       │       │ price        │
│ createdAt    │       │ stock        │
└──────────────┘       │ description  │
                       │ imageUrl     │
                       │ active       │
                       └──────────────┘
                              │
                              │
┌──────────────┐       ┌──────────────┐
│    Order     │       │  OrderItem   │
├──────────────┤       ├──────────────┤
│ id           │───┐   │ id           │
│ customerId   │   │   │ productId    │
│ status       │   └──►│ productName  │
│ subtotal     │       │ tax          │
│ tax          │       │ quantity     │
│ total        │       │ unitPrice    │
│ createdAt    │       │ subtotal     │
│ shippingAddr │       └──────────────┘
└──────────────┘
```

---

## 6. Non-Functional Requirements

### 6.1 Performance

- API response time < 200ms for 95th percentile under normal load
- Database queries optimized with appropriate indexing
- Pagination required for list endpoints

### 6.2 Reliability

- Transactional integrity for order creation (all-or-nothing)
- Graceful degradation under high load

### 6.3 Maintainability

- **Strict dependency rules** ensure that changing the DB or Framework doesn't affect business logic.

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

## 8. API Documentation (OpenAPI)

### 8.1 Swagger UI Access

| Resource | URL |
|----------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## 9. Assumptions & Constraints

### Assumptions

- Customer IDs are provided by the client (no user management in scope)
- Product images are hosted externally (URL reference only)
- Tax calculation uses a fixed rate for simplicity

### Constraints

- No authentication/authorization layer
- Single-region deployment

---

## Appendix: Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2025-11-24 | API Dev Team | Updated to Clean Architecture spec |
