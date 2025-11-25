# Clean Architecture

## Overview

Clean Architecture was introduced by Robert C. Martin (Uncle Bob) and emphasizes the **dependency rule**: source code dependencies must point inward, toward higher-level policies. The inner circles contain enterprise business rules and application business rules, while outer circles contain mechanisms and frameworks.

The key insight is that frameworks, databases, and delivery mechanisms are **details** that should be kept at arm's length from the core business logic. This allows the business rules to be tested and evolved independently of external concerns.

---

## Core Principles

1. **Dependency Rule**: Dependencies only point inward. Inner layers know nothing about outer layers.

2. **Entities**: Enterprise-wide business rules. These are the most general and highest-level rules.

3. **Use Cases**: Application-specific business rules. They orchestrate the flow of data to and from entities.

4. **Interface Adapters**: Convert data between use cases/entities and external agencies (DB, web, etc.).

5. **Frameworks & Drivers**: The outermost layer containing frameworks, tools, and delivery mechanisms.

6. **Dependency Inversion**: High-level modules don't depend on low-level modules. Both depend on abstractions.

---

## The Concentric Circles

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

---

## Target Package Structure

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

---

## Layer Descriptions

### Entities Layer (`entity/`)

The innermost layer containing enterprise-wide business rules. These are the most stable and least likely to change. Entities encapsulate the most general and high-level rules.

```java
// entity/Product.java
// Pure domain entity with business logic, NO framework dependencies
public class Product {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private Integer stock;
    private final String imageUrl;
    private final boolean active;
    private final Category category;

    // Private constructor - use factory methods
    private Product(Long id, String name, String description, BigDecimal price, 
                    Integer stock, String imageUrl, boolean active, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.active = active;
        this.category = category;
    }

    // Factory method with validation
    public static Product create(Long id, String name, String description, BigDecimal price, 
                                  Integer stock, String imageUrl, boolean active, Category category) {
        // Enforce business rules
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        return new Product(id, name, description, price, stock, imageUrl, active, category);
    }

    // Business logic methods
    public boolean isAvailable() {
        return active && stock > 0;
    }

    public boolean canFulfillQuantity(int quantity) {
        return active && stock >= quantity;
    }

    public void reduceStock(int quantity) {
        if (!canFulfillQuantity(quantity)) {
            throw new IllegalStateException(
                String.format("Cannot reduce stock by %d. Current stock: %d, Active: %s", 
                              quantity, stock, active)
            );
        }
        this.stock -= quantity;
    }

    // Immutable getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public Category getCategory() { return category; }
}
```

```java
// entity/Order.java
public class Order {
    private Long id;
    private final Long customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal total;
    private final OffsetDateTime createdAt;
    private final ShippingAddress shippingAddress;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private Order(Long id, Long customerId, OrderStatus status, List<OrderItem> items,
                  BigDecimal subtotal, BigDecimal tax, BigDecimal total,
                  OffsetDateTime createdAt, ShippingAddress shippingAddress) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.createdAt = createdAt;
        this.shippingAddress = shippingAddress;
    }

    // Factory method for creating new orders
    public static Order createNew(Long customerId, List<OrderItem> items, ShippingAddress shippingAddress) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        BigDecimal subtotal = calculateSubtotal(items);
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal total = subtotal.add(tax);

        return new Order(
            null, // ID assigned after persistence
            customerId,
            OrderStatus.PENDING,
            new ArrayList<>(items),
            subtotal,
            tax,
            total,
            OffsetDateTime.now(),
            shippingAddress
        );
    }

    // Factory method for reconstituting from persistence
    public static Order reconstitute(Long id, Long customerId, OrderStatus status, 
                                      List<OrderItem> items, BigDecimal subtotal, 
                                      BigDecimal tax, BigDecimal total,
                                      OffsetDateTime createdAt, ShippingAddress shippingAddress) {
        return new Order(id, customerId, status, items, subtotal, tax, total, createdAt, shippingAddress);
    }

    private static BigDecimal calculateSubtotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    // Business operations
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel shipped or delivered orders");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // For persistence layer to set ID
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Order already has an ID");
        }
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
}
```

```java
// entity/ShippingAddress.java (Value Object)
public record ShippingAddress(
    String street,
    String city,
    String state,
    String zipCode,
    String country
) {
    public ShippingAddress {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code is required");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
    }
}
```

### Use Cases Layer (`usecase/`)

Application-specific business rules. Use cases orchestrate the flow of data to and from entities and direct those entities to use their enterprise-wide business rules.

#### Input/Output Boundaries

```java
// usecase/category/GetAllCategoriesUseCase.java (Input Boundary)
public interface GetAllCategoriesUseCase {
    List<CategoryOutputData> execute();
}
```

```java
// usecase/category/CategoryOutputData.java (Output Data)
public record CategoryOutputData(
    Long id,
    String name,
    String description,
    boolean active,
    OffsetDateTime createdAt
) {}
```

```java
// usecase/product/SearchProductsUseCase.java
public interface SearchProductsUseCase {
    PagedProductOutputData execute(ProductSearchInputData input);
}
```

```java
// usecase/product/ProductSearchInputData.java (Input Data)
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
) {
    public ProductSearchInputData {
        if (active == null) active = true;
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null) sortDirection = "asc";
    }
}
```

```java
// usecase/product/ProductOutputData.java
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

```java
// usecase/product/PagedProductOutputData.java
public record PagedProductOutputData(
    List<ProductOutputData> content,
    long totalElements,
    int totalPages,
    int number,
    int size,
    boolean first,
    boolean last
) {}
```

```java
// usecase/order/CreateOrderUseCase.java
public interface CreateOrderUseCase {
    OrderOutputData execute(CreateOrderInputData input);
}
```

```java
// usecase/order/CreateOrderInputData.java
public record CreateOrderInputData(
    Long customerId,
    List<OrderItemInputData> items,
    String shippingStreet,
    String shippingCity,
    String shippingState,
    String shippingZipCode,
    String shippingCountry
) {}

// usecase/order/OrderItemInputData.java
public record OrderItemInputData(Long productId, Integer quantity) {}
```

```java
// usecase/order/OrderOutputData.java
public record OrderOutputData(
    Long orderId,
    String status,
    List<OrderItemOutputData> items,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total,
    OffsetDateTime createdAt
) {}

// usecase/order/OrderItemOutputData.java
public record OrderItemOutputData(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
```

#### Gateways (Output Boundaries)

```java
// usecase/gateway/CategoryGateway.java
public interface CategoryGateway {
    List<Category> findAllActive();
    Optional<Category> findById(Long id);
    boolean existsById(Long id);
}
```

```java
// usecase/gateway/ProductGateway.java
public interface ProductGateway {
    PagedResult<Product> findAll(ProductSearchInputData criteria);
    Optional<Product> findById(Long id);
    List<Product> findAllByIds(List<Long> ids);
    Product save(Product product);
}

// usecase/gateway/PagedResult.java
public record PagedResult<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int number,
    int size
) {
    public boolean isFirst() { return number == 0; }
    public boolean isLast() { return number >= totalPages - 1; }
}
```

```java
// usecase/gateway/OrderGateway.java
public interface OrderGateway {
    Order save(Order order);
    Optional<Order> findById(Long id);
}
```

#### Interactors (Use Case Implementations)

```java
// usecase/category/GetAllCategoriesInteractor.java
public class GetAllCategoriesInteractor implements GetAllCategoriesUseCase {
    
    private final CategoryGateway categoryGateway;

    public GetAllCategoriesInteractor(CategoryGateway categoryGateway) {
        this.categoryGateway = categoryGateway;
    }

    @Override
    public List<CategoryOutputData> execute() {
        return categoryGateway.findAllActive().stream()
            .map(this::toOutputData)
            .toList();
    }

    private CategoryOutputData toOutputData(Category category) {
        return new CategoryOutputData(
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
// usecase/product/SearchProductsInteractor.java
public class SearchProductsInteractor implements SearchProductsUseCase {
    
    private final ProductGateway productGateway;
    private final CategoryGateway categoryGateway;

    public SearchProductsInteractor(ProductGateway productGateway, CategoryGateway categoryGateway) {
        this.productGateway = productGateway;
        this.categoryGateway = categoryGateway;
    }

    @Override
    public PagedProductOutputData execute(ProductSearchInputData input) {
        // Validate category exists if specified
        if (input.categoryId() != null && !categoryGateway.existsById(input.categoryId())) {
            throw new EntityNotFoundException("Category not found: " + input.categoryId());
        }

        PagedResult<Product> result = productGateway.findAll(input);

        List<ProductOutputData> content = result.content().stream()
            .map(this::toOutputData)
            .toList();

        return new PagedProductOutputData(
            content,
            result.totalElements(),
            result.totalPages(),
            result.number(),
            result.size(),
            result.isFirst(),
            result.isLast()
        );
    }

    private ProductOutputData toOutputData(Product product) {
        return new ProductOutputData(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory() != null ? product.getCategory().getId() : null,
            product.getCategory() != null ? product.getCategory().getName() : null,
            product.getStock(),
            product.getImageUrl(),
            product.isActive()
        );
    }
}
```

```java
// usecase/order/CreateOrderInteractor.java
public class CreateOrderInteractor implements CreateOrderUseCase {
    
    private final ProductGateway productGateway;
    private final OrderGateway orderGateway;

    public CreateOrderInteractor(ProductGateway productGateway, OrderGateway orderGateway) {
        this.productGateway = productGateway;
        this.orderGateway = orderGateway;
    }

    @Override
    @Transactional
    public OrderOutputData execute(CreateOrderInputData input) {
        // Validate input
        if (input.items() == null || input.items().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }

        // Load products
        List<Long> productIds = input.items().stream()
            .map(OrderItemInputData::productId)
            .toList();
        
        List<Product> products = productGateway.findAllByIds(productIds);
        
        if (products.size() != productIds.size()) {
            throw new EntityNotFoundException("One or more products not found");
        }

        // Build order items using entity business logic
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemInputData itemInput : input.items()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(itemInput.productId()))
                .findFirst()
                .orElseThrow();

            // Entity validates its own rules
            if (!product.canFulfillQuantity(itemInput.quantity())) {
                throw new BusinessRuleException(
                    "Insufficient stock for product: " + product.getId()
                );
            }

            OrderItem orderItem = OrderItem.create(
                product.getId(),
                product.getName(),
                product.getPrice(),
                itemInput.quantity()
            );
            orderItems.add(orderItem);
        }

        // Create shipping address (value object validates itself)
        ShippingAddress shippingAddress = new ShippingAddress(
            input.shippingStreet(),
            input.shippingCity(),
            input.shippingState(),
            input.shippingZipCode(),
            input.shippingCountry()
        );

        // Create order (entity validates and calculates totals)
        Order order = Order.createNew(input.customerId(), orderItems, shippingAddress);

        // Persist order
        Order savedOrder = orderGateway.save(order);

        // Update product stock (entity business logic)
        for (OrderItem item : savedOrder.getItems()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(item.getProductId()))
                .findFirst()
                .orElseThrow();
            
            product.reduceStock(item.getQuantity());
            productGateway.save(product);
        }

        return toOutputData(savedOrder);
    }

    private OrderOutputData toOutputData(Order order) {
        List<OrderItemOutputData> items = order.getItems().stream()
            .map(item -> new OrderItemOutputData(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
            ))
            .toList();

        return new OrderOutputData(
            order.getId(),
            order.getStatus().name(),
            items,
            order.getSubtotal(),
            order.getTax(),
            order.getTotal(),
            order.getCreatedAt()
        );
    }
}
```

### Interface Adapters Layer (`interface_adapter/`)

This layer converts data from the format most convenient for use cases and entities to the format most convenient for external agencies.

#### Controllers

```java
// interface_adapter/controller/CategoryController.java
@Tag(name = "Categories", description = "Product category management endpoints")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    
    private final GetAllCategoriesUseCase getAllCategoriesUseCase;
    private final CategoryPresenter presenter;

    public CategoryController(GetAllCategoriesUseCase getAllCategoriesUseCase, 
                               CategoryPresenter presenter) {
        this.getAllCategoriesUseCase = getAllCategoriesUseCase;
        this.presenter = presenter;
    }

    @Operation(summary = "Get all product categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponseModel>> getAll() {
        List<CategoryOutputData> categories = getAllCategoriesUseCase.execute();
        List<CategoryResponseModel> response = presenter.present(categories);
        return ResponseEntity.ok(response);
    }
}
```

```java
// interface_adapter/controller/OrderController.java
@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final OrderPresenter presenter;

    public OrderController(CreateOrderUseCase createOrderUseCase, OrderPresenter presenter) {
        this.createOrderUseCase = createOrderUseCase;
        this.presenter = presenter;
    }

    @Operation(summary = "Create a new order")
    @PostMapping
    public ResponseEntity<OrderResponseModel> create(
            @Valid @RequestBody OrderCreateRequestModel request) {
        
        // Convert request to use case input
        CreateOrderInputData input = new CreateOrderInputData(
            request.customerId(),
            request.items().stream()
                .map(item -> new OrderItemInputData(item.productId(), item.quantity()))
                .toList(),
            request.shippingAddress().street(),
            request.shippingAddress().city(),
            request.shippingAddress().state(),
            request.shippingAddress().zipCode(),
            request.shippingAddress().country()
        );
        
        // Execute use case
        OrderOutputData output = createOrderUseCase.execute(input);
        
        // Present result
        OrderResponseModel response = presenter.present(output);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

#### Presenters

```java
// interface_adapter/presenter/CategoryPresenter.java
@Component
public class CategoryPresenter {
    
    public List<CategoryResponseModel> present(List<CategoryOutputData> categories) {
        return categories.stream()
            .map(this::toResponseModel)
            .toList();
    }

    private CategoryResponseModel toResponseModel(CategoryOutputData data) {
        return new CategoryResponseModel(
            data.id(),
            data.name(),
            data.description(),
            data.active(),
            data.createdAt()
        );
    }
}
```

```java
// interface_adapter/presenter/OrderPresenter.java
@Component
public class OrderPresenter {
    
    public OrderResponseModel present(OrderOutputData output) {
        List<OrderItemResponseModel> items = output.items().stream()
            .map(item -> new OrderItemResponseModel(
                item.productId(),
                item.productName(),
                item.quantity(),
                item.unitPrice(),
                item.subtotal()
            ))
            .toList();

        return new OrderResponseModel(
            output.orderId(),
            output.status(),
            items,
            output.subtotal(),
            output.tax(),
            output.total(),
            output.createdAt()
        );
    }
}
```

#### DTOs (View Models)

```java
// interface_adapter/dto/response/CategoryResponseModel.java
public record CategoryResponseModel(
    Long id,
    String name,
    String description,
    boolean active,
    OffsetDateTime createdAt
) {}
```

```java
// interface_adapter/dto/request/OrderCreateRequestModel.java
public record OrderCreateRequestModel(
    @NotNull Long customerId,
    @NotEmpty @Valid List<OrderItemRequestModel> items,
    @NotNull @Valid ShippingAddressRequestModel shippingAddress
) {}

// interface_adapter/dto/request/OrderItemRequestModel.java
public record OrderItemRequestModel(
    @NotNull Long productId,
    @NotNull @Min(1) @Max(99) Integer quantity
) {}

// interface_adapter/dto/request/ShippingAddressRequestModel.java
public record ShippingAddressRequestModel(
    @NotBlank @Size(max = 200) String street,
    @NotBlank @Size(max = 100) String city,
    @Size(max = 100) String state,
    @NotBlank @Size(max = 20) String zipCode,
    @NotBlank @Size(max = 100) String country
) {}
```

#### Gateway Implementations

```java
// interface_adapter/gateway/CategoryGatewayImpl.java
@Component
public class CategoryGatewayImpl implements CategoryGateway {
    
    private final CategoryJpaRepository repository;
    private final CategoryDataMapper mapper;

    public CategoryGatewayImpl(CategoryJpaRepository repository, CategoryDataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Category> findAllActive() {
        return repository.findByActiveTrue().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
```

### Frameworks & Drivers Layer (`framework/`)

The outermost layer containing all framework-specific code.

```java
// framework/persistence/entity/ProductDataEntity.java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDataEntity {
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
    private CategoryDataEntity category;
}
```

```java
// framework/persistence/mapper/ProductDataMapper.java
@Component
public class ProductDataMapper {
    
    private final CategoryDataMapper categoryMapper;

    public ProductDataMapper(CategoryDataMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public Product toDomain(ProductDataEntity entity) {
        Category category = categoryMapper.toDomain(entity.getCategory());
        return Product.create(
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

    public ProductDataEntity toDataEntity(Product product) {
        CategoryDataEntity categoryEntity = categoryMapper.toDataEntity(product.getCategory());
        return ProductDataEntity.builder()
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
// framework/config/BeanConfiguration.java
@Configuration
public class BeanConfiguration {

    @Bean
    public GetAllCategoriesUseCase getAllCategoriesUseCase(CategoryGateway categoryGateway) {
        return new GetAllCategoriesInteractor(categoryGateway);
    }

    @Bean
    public SearchProductsUseCase searchProductsUseCase(ProductGateway productGateway, 
                                                        CategoryGateway categoryGateway) {
        return new SearchProductsInteractor(productGateway, categoryGateway);
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(ProductGateway productGateway, 
                                                  OrderGateway orderGateway) {
        return new CreateOrderInteractor(productGateway, orderGateway);
    }
}
```

---

## Dependency Diagram

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

## MapStruct Integration

Clean Architecture has multiple transformation boundaries: from request models to input data, from output data to response models, from domain entities to data entities, and so on. MapStruct simplifies all of these transformations while maintaining the strict dependency rules that Clean Architecture requires.

### Adding MapStruct Dependency

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

### Interface Adapter Mappers (View Models ↔ Use Case Data)

These mappers handle the transformation between the HTTP layer's view models and the use case layer's input/output data structures.

```java
// interface_adapter/presenter/CategoryPresenter.java
@Mapper(componentModel = "spring")
public interface CategoryPresenter {
    
    // Output Data → Response Model
    CategoryResponseModel toResponseModel(CategoryOutputData outputData);
    
    List<CategoryResponseModel> toResponseModelList(List<CategoryOutputData> outputDataList);
}
```

```java
// interface_adapter/controller/OrderControllerMapper.java
@Mapper(componentModel = "spring")
public interface OrderControllerMapper {
    
    // Request Model → Input Data
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "items", target = "items")
    CreateOrderInputData toInputData(OrderCreateRequestModel request);
    
    // Nested mapping for order items
    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "quantity", target = "quantity")
    OrderItemInputData toItemInputData(OrderItemRequestModel item);
    
    // Shipping address mapping
    default CreateOrderInputData mapWithShippingAddress(OrderCreateRequestModel request) {
        return new CreateOrderInputData(
            request.customerId(),
            request.items().stream().map(this::toItemInputData).toList(),
            request.shippingAddress().street(),
            request.shippingAddress().city(),
            request.shippingAddress().state(),
            request.shippingAddress().zipCode(),
            request.shippingAddress().country()
        );
    }
}
```

```java
// interface_adapter/presenter/OrderPresenter.java
@Mapper(componentModel = "spring")
public interface OrderPresenter {
    
    // Output Data → Response Model
    @Mapping(source = "orderId", target = "orderId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "items", target = "items")
    OrderResponseModel toResponseModel(OrderOutputData outputData);
    
    // Nested item mapping
    OrderItemResponseModel toItemResponseModel(OrderItemOutputData itemData);
}
```

### Gateway Mapper (Domain Entities ↔ Data Entities)

These mappers handle the transformation between the innermost entities and the outermost data entities.

```java
// framework/persistence/mapper/CategoryDataMapper.java
@Mapper(componentModel = "spring")
public interface CategoryDataMapper {
    
    // Data Entity → Domain Entity
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "createdAt", target = "createdAt")
    Category toDomain(CategoryDataEntity dataEntity);
    
    List<Category> toDomainList(List<CategoryDataEntity> dataEntities);
    
    // Domain Entity → Data Entity
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "createdAt", target = "createdAt")
    CategoryDataEntity toDataEntity(Category category);
}
```

```java
// framework/persistence/mapper/ProductDataMapper.java
@Mapper(componentModel = "spring", uses = {CategoryDataMapper.class})
public interface ProductDataMapper {
    
    // Data Entity → Domain Entity
    // MapStruct will use CategoryDataMapper automatically for the category field
    Product toDomain(ProductDataEntity dataEntity);
    
    List<Product> toDomainList(List<ProductDataEntity> dataEntities);
    
    // Domain Entity → Data Entity
    @Mapping(target = "id", ignore = true) // ID managed by persistence
    ProductDataEntity toDataEntity(Product product);
    
    // For updates where ID should be preserved
    @Mapping(target = "id", source = "id")
    ProductDataEntity toDataEntityWithId(Product product);
}
```

```java
// framework/persistence/mapper/OrderDataMapper.java
@Mapper(componentModel = "spring", uses = {OrderItemDataMapper.class})
public interface OrderDataMapper {
    
    // Data Entity → Domain Entity
    // Use factory method to create domain object correctly
    default Order toDomain(OrderDataEntity dataEntity) {
        List<OrderItem> items = dataEntity.getItems().stream()
            .map(this::itemToDomain)
            .toList();
        
        ShippingAddress shippingAddress = new ShippingAddress(
            dataEntity.getShippingStreet(),
            dataEntity.getShippingCity(),
            dataEntity.getShippingState(),
            dataEntity.getShippingZipCode(),
            dataEntity.getShippingCountry()
        );
        
        return Order.reconstitute(
            dataEntity.getId(),
            dataEntity.getCustomerId(),
            dataEntity.getStatus(),
            items,
            dataEntity.getSubtotal(),
            dataEntity.getTax(),
            dataEntity.getTotal(),
            dataEntity.getCreatedAt(),
            shippingAddress
        );
    }
    
    // Helper method for item conversion
    default OrderItem itemToDomain(OrderItemDataEntity itemEntity) {
        return OrderItem.reconstitute(
            itemEntity.getId(),
            itemEntity.getProduct().getId(),
            itemEntity.getProduct().getName(),
            itemEntity.getUnitPrice(),
            itemEntity.getQuantity(),
            itemEntity.getSubtotal()
        );
    }
    
    // Domain Entity → Data Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "subtotal", target = "subtotal")
    @Mapping(source = "tax", target = "tax")
    @Mapping(source = "total", target = "total")
    @Mapping(source = "shippingAddress.street", target = "shippingStreet")
    @Mapping(source = "shippingAddress.city", target = "shippingCity")
    @Mapping(source = "shippingAddress.state", target = "shippingState")
    @Mapping(source = "shippingAddress.zipCode", target = "shippingZipCode")
    @Mapping(source = "shippingAddress.country", target = "shippingCountry")
    @Mapping(target = "items", ignore = true) // Handled separately
    OrderDataEntity toDataEntity(Order order);
}
```

### Using Mappers in Clean Architecture

The beauty of Clean Architecture is that mappers respect the dependency rule. Outer layers depend on inner layers through these mappers.

```java
// interface_adapter/controller/OrderController.java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final OrderPresenter presenter;
    private final OrderControllerMapper controllerMapper;

    @PostMapping
    public ResponseEntity<OrderResponseModel> create(
            @Valid @RequestBody OrderCreateRequestModel request) {
        
        // Map request to input data (outer → inner)
        CreateOrderInputData inputData = controllerMapper.mapWithShippingAddress(request);
        
        // Execute use case (stays in inner circles)
        OrderOutputData outputData = createOrderUseCase.execute(inputData);
        
        // Present result (inner → outer)
        OrderResponseModel response = presenter.toResponseModel(outputData);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

```java
// interface_adapter/gateway/ProductGatewayImpl.java
@Component
@RequiredArgsConstructor
public class ProductGatewayImpl implements ProductGateway {
    
    private final ProductJpaRepository jpaRepository;
    private final ProductDataMapper mapper;

    @Override
    public Optional<Product> findById(Long id) {
        // Data layer → Domain layer transformation
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        // Domain layer → Data layer transformation
        ProductDataEntity entity = product.getId() == null
            ? mapper.toDataEntity(product)
            : mapper.toDataEntityWithId(product);
        
        ProductDataEntity saved = jpaRepository.save(entity);
        
        // Data layer → Domain layer transformation
        return mapper.toDomain(saved);
    }
}
```

---

## Testing Strategy

Clean Architecture provides exceptional testability because of its strict separation of concerns and dependency rules. You can test each layer in isolation, and the innermost layers require no infrastructure at all. The testing strategy follows the same circular structure as the architecture itself, with the most critical business logic tests being the fastest and most isolated.

### Test Dependencies

Add to your `build.gradle`:

```gradle
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.7.0'
}
```

### Entity Layer Tests (Innermost Circle)

These are the fastest tests you will write because entities have zero dependencies. They verify your core business rules using pure Java with no frameworks, no mocks, nothing but logic.

```java
// entity/ProductTest.java
class ProductTest {
    
    @Test
    void create_withValidData_createsProduct() {
        // Given
        Category category = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        
        // When
        Product product = Product.create(
            1L,
            "Gaming Laptop",
            "High-performance laptop",
            new BigDecimal("1299.99"),
            10,
            "laptop.jpg",
            true,
            category
        );
        
        // Then
        assertNotNull(product);
        assertEquals("Gaming Laptop", product.getName());
        assertEquals(new BigDecimal("1299.99"), product.getPrice());
        assertTrue(product.isActive());
    }
    
    @Test
    void create_withNullName_throwsException() {
        // Given
        Category category = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            Product.create(1L, null, "Description", BigDecimal.TEN, 5, "img.jpg", true, category)
        );
    }
    
    @Test
    void create_withNegativePrice_throwsException() {
        // Given
        Category category = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            Product.create(1L, "Product", "Description", new BigDecimal("-10"), 5, "img.jpg", true, category)
        );
    }
    
    @Test
    void reduceStock_withSufficientQuantity_reducesSuccessfully() {
        // Given
        Category category = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        Product product = Product.create(1L, "Laptop", "Desc", BigDecimal.TEN, 10, "img.jpg", true, category);
        
        // When
        product.reduceStock(3);
        
        // Then
        assertEquals(7, product.getStock());
    }
    
    @Test
    void reduceStock_withInsufficientQuantity_throwsException() {
        // Given
        Category category = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        Product product = Product.create(1L, "Laptop", "Desc", BigDecimal.TEN, 5, "img.jpg", true, category);
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> product.reduceStock(10));
    }
}
```

```java
// entity/OrderTest.java
class OrderTest {
    
    @Test
    void createNew_withValidData_calculatesTotalsCorrectly() {
        // Given
        OrderItem item1 = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 2);
        OrderItem item2 = OrderItem.create(2L, "Mouse", new BigDecimal("50.00"), 1);
        List<OrderItem> items = List.of(item1, item2);
        
        ShippingAddress address = new ShippingAddress(
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        // When
        Order order = Order.createNew(1L, items, address);
        
        // Then
        assertEquals(new BigDecimal("2050.00"), order.getSubtotal());
        assertEquals(new BigDecimal("164.00"), order.getTax()); // 8% of 2050
        assertEquals(new BigDecimal("2214.00"), order.getTotal());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
    }
    
    @Test
    void createNew_withEmptyItems_throwsException() {
        // Given
        ShippingAddress address = new ShippingAddress(
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            Order.createNew(1L, List.of(), address)
        );
    }
    
    @Test
    void createNew_withNullShippingAddress_throwsException() {
        // Given
        OrderItem item = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 1);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            Order.createNew(1L, List.of(item), null)
        );
    }
}
```

### Use Case Tests (Application Business Rules)

These tests verify the orchestration logic in your interactors. You mock the gateways to isolate the use case logic from infrastructure concerns.

```java
// usecase/order/CreateOrderInteractorTest.java
@ExtendWith(MockitoExtension.class)
class CreateOrderInteractorTest {
    
    @Mock
    private ProductGateway productGateway;
    
    @Mock
    private OrderGateway orderGateway;
    
    private CreateOrderInteractor interactor;
    
    @BeforeEach
    void setUp() {
        interactor = new CreateOrderInteractor(productGateway, orderGateway);
    }
    
    @Test
    void execute_withValidInput_createsOrderAndUpdatesStock() {
        // Given
        Category category = Category.create(1L, "Electronics", "desc", true, OffsetDateTime.now());
        Product product = Product.create(
            1L, "Laptop", "Gaming laptop", 
            new BigDecimal("1000.00"), 10, "img.jpg", true, category
        );
        
        CreateOrderInputData input = new CreateOrderInputData(
            1L,
            List.of(new OrderItemInputData(1L, 2)),
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        when(productGateway.findAllByIds(anyList())).thenReturn(List.of(product));
        when(orderGateway.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.assignId(100L);
            return order;
        });
        
        // When
        OrderOutputData result = interactor.execute(input);
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.orderId());
        assertEquals("PENDING", result.status());
        assertEquals(2, result.items().size());
        
        verify(productGateway).findAllByIds(List.of(1L));
        verify(orderGateway).save(any(Order.class));
        verify(productGateway).save(argThat(p -> p.getStock() == 8)); // Verify stock reduction
    }
    
    @Test
    void execute_withInsufficientStock_throwsException() {
        // Given
        Category category = Category.create(1L, "Electronics", "desc", true, OffsetDateTime.now());
        Product product = Product.create(
            1L, "Laptop", "Gaming laptop", 
            new BigDecimal("1000.00"), 1, "img.jpg", true, category
        );
        
        CreateOrderInputData input = new CreateOrderInputData(
            1L,
            List.of(new OrderItemInputData(1L, 5)), // Requesting more than available
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        when(productGateway.findAllByIds(anyList())).thenReturn(List.of(product));
        
        // When & Then
        assertThrows(BusinessRuleException.class, () -> interactor.execute(input));
        verify(orderGateway, never()).save(any());
    }
    
    @Test
    void execute_withInactiveProduct_throwsException() {
        // Given
        Category category = Category.create(1L, "Electronics", "desc", true, OffsetDateTime.now());
        Product product = Product.create(
            1L, "Laptop", "Gaming laptop", 
            new BigDecimal("1000.00"), 10, "img.jpg", false, category // Inactive!
        );
        
        CreateOrderInputData input = new CreateOrderInputData(
            1L,
            List.of(new OrderItemInputData(1L, 2)),
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        when(productGateway.findAllByIds(anyList())).thenReturn(List.of(product));
        
        // When & Then
        assertThrows(BusinessRuleException.class, () -> interactor.execute(input));
    }
}
```

### Gateway Implementation Tests (Infrastructure Layer)

These integration tests verify that your gateway implementations correctly interact with the database and perform the necessary transformations between entities and data entities.

```java
// interface_adapter/gateway/ProductGatewayImplTest.java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ProductGatewayImpl.class, ProductDataMapperImpl.class, CategoryDataMapperImpl.class})
class ProductGatewayImplTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private ProductGatewayImpl gateway;
    
    @Autowired
    private ProductJpaRepository jpaRepository;
    
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    
    @Test
    void findById_existingProduct_returnsProduct() {
        // Given - create test data in database
        CategoryDataEntity categoryEntity = categoryJpaRepository.save(
            CategoryDataEntity.builder()
                .name("Electronics")
                .description("Electronic devices")
                .active(true)
                .build()
        );
        
        ProductDataEntity productEntity = jpaRepository.save(
            ProductDataEntity.builder()
                .name("Gaming Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("1299.99"))
                .stock(10)
                .imageUrl("laptop.jpg")
                .active(true)
                .category(categoryEntity)
                .build()
        );
        
        // When - use gateway (returns domain entity)
        Optional<Product> result = gateway.findById(productEntity.getId());
        
        // Then - verify domain entity
        assertTrue(result.isPresent());
        Product product = result.get();
        assertEquals("Gaming Laptop", product.getName());
        assertEquals(new BigDecimal("1299.99"), product.getPrice());
        assertEquals(10, product.getStock());
        assertTrue(product.isActive());
    }
    
    @Test
    void save_newProduct_persistsAndReturnsWithId() {
        // Given - create domain entity
        Category category = Category.create(1L, "Electronics", "desc", true, OffsetDateTime.now());
        Product product = Product.create(
            null, // No ID yet
            "Wireless Mouse",
            "Ergonomic wireless mouse",
            new BigDecimal("39.99"),
            50,
            "mouse.jpg",
            true,
            category
        );
        
        // When
        Product saved = gateway.save(product);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("Wireless Mouse", saved.getName());
        
        // Verify it's actually in the database
        Optional<ProductDataEntity> inDb = jpaRepository.findById(saved.getId());
        assertTrue(inDb.isPresent());
        assertEquals("Wireless Mouse", inDb.get().getName());
    }
}
```

### Controller Tests (Interface Adapters)

These tests verify that your controllers properly handle HTTP requests and transform between view models and use case input/output data.

```java
// interface_adapter/controller/CategoryControllerTest.java
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GetAllCategoriesUseCase getAllCategoriesUseCase;
    
    @MockBean
    private CategoryPresenter presenter;
    
    @Test
    void getAll_returnsCategories() throws Exception {
        // Given
        CategoryOutputData outputData = new CategoryOutputData(
            1L, "Electronics", "Electronic devices", true, OffsetDateTime.now()
        );
        CategoryResponseModel responseModel = new CategoryResponseModel(
            1L, "Electronics", "Electronic devices", true, OffsetDateTime.now()
        );
        
        when(getAllCategoriesUseCase.execute()).thenReturn(List.of(outputData));
        when(presenter.toResponseModel(outputData)).thenReturn(responseModel);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[0].active").value(true));
        
        verify(getAllCategoriesUseCase).execute();
        verify(presenter).toResponseModel(outputData);
    }
}
```

### Full Integration Tests

These tests verify the entire application stack working together, exercising all layers from HTTP request to database and back.

```java
// integration/CreateOrderFullStackTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CreateOrderFullStackTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductJpaRepository productRepository;
    
    @Autowired
    private CategoryJpaRepository categoryRepository;
    
    @BeforeEach
    void setUp() {
        CategoryDataEntity category = categoryRepository.save(
            CategoryDataEntity.builder()
                .name("Electronics")
                .description("Electronic devices")
                .active(true)
                .build()
        );
        
        productRepository.save(
            ProductDataEntity.builder()
                .name("Gaming Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("1299.99"))
                .stock(10)
                .active(true)
                .category(category)
                .build()
        );
    }
    
    @Test
    void createOrder_withValidData_returnsCreatedOrder() {
        // Given
        OrderCreateRequestModel request = new OrderCreateRequestModel(
            1L,
            List.of(new OrderItemRequestModel(1L, 2)),
            new ShippingAddressRequestModel("123 Main St", "Springfield", "IL", "62701", "USA")
        );
        
        // When
        ResponseEntity<OrderResponseModel> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            OrderResponseModel.class
        );
        
        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().orderId());
        assertEquals("PENDING", response.getBody().status());
        assertTrue(response.getBody().total().compareTo(BigDecimal.ZERO) > 0);
        
        // Verify stock was updated in database
        ProductDataEntity product = productRepository.findById(1L).orElseThrow();
        assertEquals(8, product.getStock()); // 10 - 2
    }
}
```

### Architecture Tests

Use ArchUnit to enforce Clean Architecture's dependency rules automatically.

```java
// architecture/CleanArchitectureTest.java
class CleanArchitectureTest {
    
    private static final String BASE_PACKAGE = "app.quantun.architecture";
    private final JavaClasses classes = new ClassFileImporter()
        .importPackages(BASE_PACKAGE);
    
    @Test
    void entitiesShouldNotDependOnUseCases() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat()
            .resideInAPackage("..usecase..");
        
        rule.check(classes);
    }
    
    @Test
    void entitiesShouldNotDependOnInterfaceAdapters() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..interface_adapter..", "..controller..", "..presenter..");
        
        rule.check(classes);
    }
    
    @Test
    void entitiesShouldNotDependOnFrameworks() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..framework..", "org.springframework..");
        
        rule.check(classes);
    }
    
    @Test
    void useCasesShouldNotDependOnInterfaceAdapters() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..usecase..")
            .should().dependOnClassesThat()
            .resideInAPackage("..interface_adapter..");
        
        rule.check(classes);
    }
    
    @Test
    void useCasesShouldNotDependOnFrameworks() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..usecase..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..framework..", "org.springframework..")
            .because("Use cases should be framework-independent");
        
        rule.check(classes);
    }
    
    @Test
    void gatewaysShouldBeInterfaces() {
        ArchRule rule = classes()
            .that().resideInAPackage("..usecase.gateway..")
            .should().beInterfaces()
            .because("Gateways define contracts, implementations live in outer layers");
        
        rule.check(classes);
    }
}
```

### Test Organization

Your test directory should mirror the concentric circles of Clean Architecture:

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

### Testing Benefits in Clean Architecture

Clean Architecture provides exceptional testability through its dependency rule. The innermost entities can be tested with pure unit tests that run in milliseconds. Use cases can be tested by mocking only the gateway interfaces, keeping tests fast and focused. Interface adapters and frameworks are tested with integration tests that verify infrastructure concerns. This layered testing approach means you catch bugs early in fast tests, reserve expensive integration tests for infrastructure validation, and maintain confidence in your core business logic regardless of framework changes.

---

## Key Differences from Hexagonal

| Aspect | Clean Architecture | Hexagonal |
|--------|-------------------|-----------|
| Terminology | Entities, Use Cases, Interface Adapters | Domain, Application, Adapters |
| Focus | Dependency rule, circles | Ports and Adapters metaphor |
| Presenters | Explicit presenter pattern | Often merged with controller |
| Data Structures | Input/Output Data for boundaries | Command/Query objects |
| Configuration | Often manual bean wiring | Spring auto-wiring more common |

---

## Notes for AI Agent

When refactoring to Clean Architecture:

1. **Preserve All API Contracts**: Request/response formats must remain identical.
2. **Entities Are NOT JPA Entities**: Separate domain entities from persistence entities.
3. **Use Cases Return Output Data**: Not domain entities directly.
4. **Controllers Use Presenters**: For transforming output data to response models.
5. **Gateways Are Interfaces**: Defined in use case layer, implemented in interface adapter layer.
6. **Interactors Have No Annotations**: Pure Java classes, no Spring annotations.
7. **BeanConfiguration Wires Use Cases**: Manual bean creation in config class.
8. **Preserve Validation**: In request DTOs (framework level) and entities (domain level).
9. **Transaction Boundaries**: On interactor methods, using @Transactional.
10. **DataInitializer**: Must be updated to use gateway implementations.