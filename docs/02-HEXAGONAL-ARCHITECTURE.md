# Hexagonal Architecture (Ports & Adapters)

## Overview

Hexagonal Architecture, also known as Ports and Adapters, was introduced by Alistair Cockburn. The core idea is to isolate the application's business logic from external concerns (databases, web frameworks, external services) by defining clear boundaries through **ports** (interfaces) and **adapters** (implementations).

The business logic sits at the center of a "hexagon" and communicates with the outside world only through well-defined ports. This makes the application highly testable, flexible, and independent of frameworks.

---

## Core Principles

1. **Domain at the Center**: Business logic is isolated and has no dependencies on external frameworks or infrastructure.

2. **Ports as Boundaries**: Interfaces define how the application interacts with the outside world. There are two types:
   - **Driving Ports (Inbound)**: Define what the application CAN DO (use cases)
   - **Driven Ports (Outbound)**: Define what the application NEEDS (repositories, external services)

3. **Adapters as Implementations**: Concrete implementations that connect ports to external systems:
   - **Driving Adapters**: REST controllers, CLI commands, message consumers
   - **Driven Adapters**: JPA repositories, HTTP clients, message publishers

4. **Dependency Rule**: Dependencies always point inward. Adapters depend on ports, ports are part of the application core.

---

## Target Package Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── application/                              # APPLICATION LAYER
│   ├── port/
│   │   ├── in/                              # Driving ports (use cases)
│   │   │   ├── GetCategoriesUseCase.java
│   │   │   ├── SearchProductsUseCase.java
│   │   │   └── CreateOrderUseCase.java
│   │   └── out/                             # Driven ports (required interfaces)
│   │       ├── CategoryRepositoryPort.java
│   │       ├── ProductRepositoryPort.java
│   │       └── OrderRepositoryPort.java
│   └── service/                             # Use case implementations
│       ├── CategoryServiceImpl.java
│       ├── ProductServiceImpl.java
│       └── OrderServiceImpl.java
│
├── domain/                                   # DOMAIN LAYER (pure business logic)
│   ├── model/
│   │   ├── Category.java                    # Domain entity (no JPA annotations)
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── exception/
│   │   ├── DomainException.java
│   │   ├── InsufficientStockException.java
│   │   └── ProductNotActiveException.java
│   └── service/                             # Domain services (optional)
│       └── OrderDomainService.java
│
├── adapter/                                  # ADAPTERS LAYER
│   ├── in/                                  # Driving adapters
│   │   └── web/
│   │       ├── CategoryController.java
│   │       ├── ProductController.java
│   │       ├── OrderController.java
│   │       ├── dto/
│   │       │   ├── CategoryResponse.java
│   │       │   ├── ProductResponse.java
│   │       │   ├── ProductFilterRequest.java
│   │       │   ├── OrderCreateRequest.java
│   │       │   ├── OrderItemRequest.java
│   │       │   ├── OrderResponse.java
│   │       │   ├── OrderItemResponse.java
│   │       │   └── ShippingAddressRequest.java
│   │       ├── mapper/
│   │       │   ├── CategoryWebMapper.java
│   │       │   ├── ProductWebMapper.java
│   │       │   └── OrderWebMapper.java
│   │       └── exception/
│   │           └── WebExceptionHandler.java
│   └── out/                                 # Driven adapters
│       └── persistence/
│           ├── entity/                      # JPA entities
│           │   ├── CategoryJpaEntity.java
│           │   ├── ProductJpaEntity.java
│           │   ├── OrderJpaEntity.java
│           │   └── OrderItemJpaEntity.java
│           ├── repository/                  # Spring Data repositories
│           │   ├── CategoryJpaRepository.java
│           │   ├── ProductJpaRepository.java
│           │   ├── OrderJpaRepository.java
│           │   └── OrderItemJpaRepository.java
│           ├── adapter/                     # Port implementations
│           │   ├── CategoryRepositoryAdapter.java
│           │   ├── ProductRepositoryAdapter.java
│           │   └── OrderRepositoryAdapter.java
│           ├── mapper/
│           │   ├── CategoryPersistenceMapper.java
│           │   ├── ProductPersistenceMapper.java
│           │   └── OrderPersistenceMapper.java
│           └── specification/
│               └── ProductJpaSpecifications.java
│
└── config/                                   # CONFIGURATION
    ├── OpenApiConfig.java
    ├── BeanConfig.java                      # Wire adapters to ports
    └── DataInitializer.java
```

---

## Layer Descriptions

### Domain Layer (`domain/`)

The innermost layer containing pure business logic with **zero external dependencies**. No Spring, no JPA, no Lombok annotations that leak infrastructure concerns.

```java
// domain/model/Product.java
// Pure domain entity - NO JPA annotations
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private boolean active;
    private Category category;

    // Constructor with validation
    public Product(Long id, String name, String description, BigDecimal price, 
                   Integer stock, String imageUrl, boolean active, Category category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be non-negative");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.active = active;
        this.category = category;
    }

    // Business methods
    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    public boolean hasEnoughStock(int quantity) {
        return stock != null && stock >= quantity;
    }

    public void decrementStock(int quantity) {
        if (!hasEnoughStock(quantity)) {
            throw new InsufficientStockException(
                "Product " + id + " has insufficient stock. Available: " + stock + ", Requested: " + quantity
            );
        }
        this.stock -= quantity;
    }

    // Getters (no setters - encourage immutability or controlled mutation)
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public boolean isActive() { return active; }
    public Category getCategory() { return category; }
    // ... other getters
}
```

```java
// domain/model/Order.java
public class Order {
    private Long id;
    private Long customerId;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private OffsetDateTime createdAt;
    private ShippingAddress shippingAddress;
    private List<OrderItem> items;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    // Factory method for creating new orders
    public static Order create(Long customerId, List<OrderItem> items, ShippingAddress shippingAddress) {
        Order order = new Order();
        order.customerId = customerId;
        order.status = OrderStatus.PENDING;
        order.items = new ArrayList<>(items);
        order.shippingAddress = shippingAddress;
        order.createdAt = OffsetDateTime.now();
        order.calculateTotals();
        return order;
    }

    private void calculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        this.tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        this.total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    // Getters
    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    // ... other getters

    // Package-private setter for persistence adapter to set ID after save
    void setId(Long id) { this.id = id; }
}
```

```java
// domain/exception/InsufficientStockException.java
public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

### Application Layer (`application/`)

Contains use case definitions (ports) and their implementations (services). Orchestrates domain objects to fulfill use cases.

#### Driving Ports (Inbound)

```java
// application/port/in/GetCategoriesUseCase.java
public interface GetCategoriesUseCase {
    List<Category> getAllActiveCategories();
}
```

```java
// application/port/in/SearchProductsUseCase.java
public interface SearchProductsUseCase {
    Page<Product> searchProducts(ProductSearchCriteria criteria, int page, int size, String sortBy, String sortDirection);
}

// Supporting class for search criteria
public record ProductSearchCriteria(
    Long categoryId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean inStock,
    Boolean active
) {
    public ProductSearchCriteria {
        // Default active to true if not specified
        if (active == null) {
            active = true;
        }
    }
}
```

```java
// application/port/in/CreateOrderUseCase.java
public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}

// Command object for creating orders
public record CreateOrderCommand(
    Long customerId,
    List<OrderItemCommand> items,
    ShippingAddress shippingAddress
) {}

public record OrderItemCommand(Long productId, Integer quantity) {}
```

#### Driven Ports (Outbound)

```java
// application/port/out/CategoryRepositoryPort.java
public interface CategoryRepositoryPort {
    List<Category> findAllActive();
    Optional<Category> findById(Long id);
    boolean existsById(Long id);
}
```

```java
// application/port/out/ProductRepositoryPort.java
public interface ProductRepositoryPort {
    Page<Product> findAll(ProductSearchCriteria criteria, int page, int size, String sortBy, String sortDirection);
    Optional<Product> findById(Long id);
    List<Product> findAllByIds(List<Long> ids);
    Product save(Product product);
}
```

```java
// application/port/out/OrderRepositoryPort.java
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
}
```

#### Service Implementations

```java
// application/service/CategoryServiceImpl.java
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements GetCategoriesUseCase {
    
    private final CategoryRepositoryPort categoryRepository;

    @Override
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllActive();
    }
}
```

```java
// application/service/ProductServiceImpl.java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements SearchProductsUseCase {
    
    private final ProductRepositoryPort productRepository;
    private final CategoryRepositoryPort categoryRepository;

    @Override
    public Page<Product> searchProducts(ProductSearchCriteria criteria, int page, int size, 
                                         String sortBy, String sortDirection) {
        // Validate category exists if specified
        if (criteria.categoryId() != null && !categoryRepository.existsById(criteria.categoryId())) {
            throw new CategoryNotFoundException("Category not found: " + criteria.categoryId());
        }
        
        return productRepository.findAll(criteria, page, size, sortBy, sortDirection);
    }
}
```

```java
// application/service/OrderServiceImpl.java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements CreateOrderUseCase {
    
    private final ProductRepositoryPort productRepository;
    private final OrderRepositoryPort orderRepository;

    @Override
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // Load all products
        List<Long> productIds = command.items().stream()
            .map(OrderItemCommand::productId)
            .toList();
        
        List<Product> products = productRepository.findAllByIds(productIds);
        
        // Validate all products exist
        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException("One or more products not found");
        }
        
        // Create order items and validate stock
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemCommand itemCmd : command.items()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(itemCmd.productId()))
                .findFirst()
                .orElseThrow();
            
            // Domain validation through domain objects
            if (!product.isActive()) {
                throw new ProductNotActiveException("Product is inactive: " + product.getId());
            }
            
            // Create order item (validates stock in domain)
            OrderItem orderItem = OrderItem.create(product, itemCmd.quantity());
            orderItems.add(orderItem);
        }
        
        // Create order using domain factory method
        Order order = Order.create(command.customerId(), orderItems, command.shippingAddress());
        
        // Persist order
        Order savedOrder = orderRepository.save(order);
        
        // Decrement stock (domain logic)
        for (OrderItem item : savedOrder.getItems()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(item.getProductId()))
                .findFirst()
                .orElseThrow();
            
            product.decrementStock(item.getQuantity());
            productRepository.save(product);
        }
        
        return savedOrder;
    }
}
```

### Adapter Layer (`adapter/`)

#### Driving Adapters (Inbound) - Web

```java
// adapter/in/web/CategoryController.java
@Tag(name = "Categories", description = "Product category management endpoints")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final GetCategoriesUseCase getCategoriesUseCase;
    private final CategoryWebMapper mapper;

    @Operation(summary = "Get all product categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all categories"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<Category> categories = getCategoriesUseCase.getAllActiveCategories();
        List<CategoryResponse> response = categories.stream()
            .map(mapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }
}
```

```java
// adapter/in/web/dto/CategoryResponse.java
public record CategoryResponse(
    Long id,
    String name,
    String description,
    boolean active,
    OffsetDateTime createdAt
) {}
```

```java
// adapter/in/web/mapper/CategoryWebMapper.java
@Component
public class CategoryWebMapper {
    
    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.isActive(),
            category.getCreatedAt()
        );
    }
}
```

```java
// adapter/in/web/OrderController.java
@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderWebMapper mapper;

    @Operation(summary = "Create a new order")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        // Map web request to application command
        CreateOrderCommand command = mapper.toCommand(request);
        
        // Execute use case
        Order order = createOrderUseCase.createOrder(command);
        
        // Map domain result to web response
        OrderResponse response = mapper.toResponse(order);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

```java
// adapter/in/web/exception/WebExceptionHandler.java
@RestControllerAdvice
public class WebExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Object> handleCategoryNotFound(CategoryNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStock(InsufficientStockException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ProductNotActiveException.class)
    public ResponseEntity<Object> handleProductNotActive(ProductNotActiveException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
```

#### Driven Adapters (Outbound) - Persistence

```java
// adapter/out/persistence/entity/ProductJpaEntity.java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity category;
}
```

```java
// adapter/out/persistence/repository/ProductJpaRepository.java
public interface ProductJpaRepository extends 
    JpaRepository<ProductJpaEntity, Long>, 
    JpaSpecificationExecutor<ProductJpaEntity> {
}
```

```java
// adapter/out/persistence/adapter/ProductRepositoryAdapter.java
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;
    private final ProductPersistenceMapper mapper;

    @Override
    public Page<Product> findAll(ProductSearchCriteria criteria, int page, int size, 
                                  String sortBy, String sortDirection) {
        Specification<ProductJpaEntity> spec = ProductJpaSpecifications.fromCriteria(criteria);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductJpaEntity> entityPage = jpaRepository.findAll(spec, pageable);
        
        return entityPage.map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toEntity(product);
        ProductJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

```java
// adapter/out/persistence/mapper/ProductPersistenceMapper.java
@Component
@RequiredArgsConstructor
public class ProductPersistenceMapper {
    
    private final CategoryPersistenceMapper categoryMapper;

    public Product toDomain(ProductJpaEntity entity) {
        Category category = categoryMapper.toDomain(entity.getCategory());
        return new Product(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getStock(),
            entity.getImageUrl(),
            entity.isActive(),
            category
        );
    }

    public ProductJpaEntity toEntity(Product product) {
        CategoryJpaEntity categoryEntity = categoryMapper.toEntity(product.getCategory());
        return ProductJpaEntity.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .imageUrl(product.getImageUrl())
            .active(product.isActive())
            .category(categoryEntity)
            .build();
    }
}
```

```java
// adapter/out/persistence/specification/ProductJpaSpecifications.java
public final class ProductJpaSpecifications {
    
    private ProductJpaSpecifications() {}

    public static Specification<ProductJpaEntity> fromCriteria(ProductSearchCriteria criteria) {
        return Specification.allOf(
            hasCategory(criteria.categoryId()),
            nameLike(criteria.name()),
            priceBetween(criteria.minPrice(), criteria.maxPrice()),
            inStock(criteria.inStock()),
            isActive(criteria.active())
        );
    }

    private static Specification<ProductJpaEntity> hasCategory(Long categoryId) {
        return (root, query, cb) -> 
            categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<ProductJpaEntity> nameLike(String name) {
        return (root, query, cb) ->
            (name == null || name.isBlank()) ? null : 
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<ProductJpaEntity> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    private static Specification<ProductJpaEntity> inStock(Boolean inStock) {
        return (root, query, cb) -> 
            (inStock == null || !inStock) ? null : cb.greaterThan(root.get("stock"), 0);
    }

    private static Specification<ProductJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> 
            active == null ? null : cb.equal(root.get("active"), active);
    }
}
```

---

## Dependency Diagram

```
                    ┌─────────────────────────────────────────┐
                    │           DRIVING ADAPTERS              │
                    │                                         │
                    │   ┌─────────────────────────────────┐   │
                    │   │   adapter/in/web/               │   │
                    │   │   (Controllers, DTOs)           │   │
                    │   └─────────────┬───────────────────┘   │
                    │                 │                       │
                    └─────────────────┼───────────────────────┘
                                      │ uses
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                                │
│                                                                          │
│  ┌──────────────────────────┐      ┌──────────────────────────────────┐ │
│  │   application/port/in/   │      │   application/service/           │ │
│  │   (Use Case Interfaces)  │◄─────│   (Use Case Implementations)     │ │
│  │                          │      │                                  │ │
│  │   - GetCategoriesUseCase │      │   - CategoryServiceImpl          │ │
│  │   - SearchProductsUseCase│      │   - ProductServiceImpl           │ │
│  │   - CreateOrderUseCase   │      │   - OrderServiceImpl             │ │
│  └──────────────────────────┘      └──────────────┬───────────────────┘ │
│                                                   │                      │
│                                                   │ uses                 │
│                                                   ▼                      │
│                                    ┌──────────────────────────────────┐ │
│                                    │   application/port/out/         │ │
│                                    │   (Repository Interfaces)       │ │
│                                    │                                  │ │
│                                    │   - CategoryRepositoryPort      │ │
│                                    │   - ProductRepositoryPort       │ │
│                                    │   - OrderRepositoryPort         │ │
│                                    └──────────────┬───────────────────┘ │
└───────────────────────────────────────────────────┼─────────────────────┘
                                                    │ implemented by
                    ┌───────────────────────────────┼─────────────────────┐
                    │                               ▼                     │
                    │           DRIVEN ADAPTERS                           │
                    │                                                     │
                    │   ┌─────────────────────────────────────────────┐   │
                    │   │   adapter/out/persistence/                 │   │
                    │   │   (JPA Entities, Repositories, Adapters)   │   │
                    │   └─────────────────────────────────────────────┘   │
                    │                                                     │
                    └─────────────────────────────────────────────────────┘
                                                    │
                                                    ▼
                                        ┌───────────────────┐
                                        │     Database      │
                                        │   (PostgreSQL)    │
                                        └───────────────────┘

                    ┌─────────────────────────────────────────┐
                    │              DOMAIN LAYER               │
                    │   (Used by Application Layer)           │
                    │                                         │
                    │   domain/model/                         │
                    │   - Category, Product, Order            │
                    │   - OrderItem, OrderStatus              │
                    │                                         │
                    │   domain/exception/                     │
                    │   - DomainException                     │
                    │   - InsufficientStockException          │
                    └─────────────────────────────────────────┘
```

---

## Migration Steps from Layered Architecture

### Step 1: Create Domain Layer

1. Create `domain/model/` package.
2. Copy entities but **remove all JPA annotations**.
3. Add business logic methods to domain entities.
4. Create domain exceptions in `domain/exception/`.

### Step 2: Create Application Layer Ports

1. Create `application/port/in/` for use case interfaces.
2. Create `application/port/out/` for repository interfaces.
3. Define command/query objects for use cases.

### Step 3: Create Application Layer Services

1. Create `application/service/` implementations.
2. Implement use case interfaces using repository ports.
3. Move business logic from old services here.

### Step 4: Create Persistence Adapters

1. Create `adapter/out/persistence/entity/` with JPA entities (rename from domain).
2. Create `adapter/out/persistence/repository/` with Spring Data interfaces.
3. Create `adapter/out/persistence/adapter/` implementing repository ports.
4. Create `adapter/out/persistence/mapper/` for entity ↔ domain mapping.
5. Move specifications to `adapter/out/persistence/specification/`.

### Step 5: Create Web Adapters

1. Create `adapter/in/web/` with controllers.
2. Create `adapter/in/web/dto/` with request/response DTOs.
3. Create `adapter/in/web/mapper/` for DTO ↔ domain mapping.
4. Create `adapter/in/web/exception/` for exception handling.

### Step 6: Wire Everything Together

1. Update `config/` with necessary beans.
2. Ensure adapters are discovered by component scanning.
3. Update `DataInitializer` to use new structure.

---

## Testing Strategy

The hexagonal architecture enables isolated testing at each layer.

### Domain Layer Tests (Unit Tests)

```java
class ProductTest {
    @Test
    void decrementStock_withSufficientStock_shouldDecrease() {
        Product product = new Product(1L, "Test", "Desc", BigDecimal.TEN, 10, null, true, null);
        product.decrementStock(3);
        assertEquals(7, product.getStock());
    }

    @Test
    void decrementStock_withInsufficientStock_shouldThrow() {
        Product product = new Product(1L, "Test", "Desc", BigDecimal.TEN, 2, null, true, null);
        assertThrows(InsufficientStockException.class, () -> product.decrementStock(5));
    }
}
```

### Application Layer Tests (Unit Tests with Mocks)

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    
    @Mock
    private ProductRepositoryPort productRepository;
    
    @Mock
    private OrderRepositoryPort orderRepository;
    
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_withValidProducts_shouldCreateOrder() {
        // Given
        Product product = new Product(1L, "Test", "Desc", BigDecimal.TEN, 10, null, true, null);
        when(productRepository.findAllByIds(anyList())).thenReturn(List.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        CreateOrderCommand command = new CreateOrderCommand(
            1L,
            List.of(new OrderItemCommand(1L, 2)),
            new ShippingAddress("123 Main", "City", "ST", "12345", "USA")
        );
        
        // When
        Order result = orderService.createOrder(command);
        
        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
    }
}
```

### Adapter Tests (Integration Tests)

```java
@DataJpaTest
class ProductRepositoryAdapterTest {
    
    @Autowired
    private ProductJpaRepository jpaRepository;
    
    private ProductRepositoryAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new ProductRepositoryAdapter(jpaRepository, new ProductPersistenceMapper());
    }

    @Test
    void findAll_withCriteria_shouldFilterCorrectly() {
        // Given - test data in database
        
        // When
        ProductSearchCriteria criteria = new ProductSearchCriteria(1L, null, null, null, true, true);
        Page<Product> result = adapter.findAll(criteria, 0, 10, "name", "asc");
        
        // Then
        assertFalse(result.isEmpty());
    }
}
```

---

## Key Benefits

1. **Testability**: Domain and application logic can be tested without infrastructure.
2. **Flexibility**: Swap databases, web frameworks, or external services without changing business logic.
3. **Clear Boundaries**: Explicit interfaces define what the application does and needs.
4. **Framework Independence**: Domain layer has zero framework dependencies.
5. **Maintainability**: Changes in one adapter don't affect others.

---

## Notes for AI Agent

When refactoring to Hexagonal Architecture:

1. **Preserve All API Contracts**: Request/response formats must remain identical.
2. **Preserve Database Schema**: JPA entities should produce the same tables.
3. **Domain Entities Are NOT JPA Entities**: Create separate classes.
4. **Mappers Are Required**: Between web DTOs ↔ domain and domain ↔ JPA entities.
5. **Ports Are Interfaces**: Never concrete classes.
6. **Use Cases Are Single-Purpose**: One interface per use case.
7. **Keep OpenAPI Annotations**: On web adapter controllers.
8. **Preserve Validation**: Both in web DTOs and domain entities.
9. **Transaction Boundaries**: Stay in application service layer.
10. **DataInitializer**: Must be updated to use repository adapters.
