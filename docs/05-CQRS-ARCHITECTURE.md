# CQRS (Command Query Responsibility Segregation)

## Overview

CQRS is an architectural pattern that separates read operations (queries) from write operations (commands) into distinct models. Rather than using the same data model for both reading and writing, CQRS maintains separate models optimized for each purpose. This separation allows each side to be scaled, optimized, and evolved independently.

The fundamental insight is that read and write operations often have very different requirements: reads need to be fast and may benefit from denormalized data, while writes need to maintain consistency and enforce business rules. By separating them, you can optimize each for its specific purpose.

---

## Core Principles

1. **Commands Change State**: Commands are operations that modify data. They don't return data (except success/failure). Commands are named with imperative verbs (e.g., `CreateOrder`, `UpdateProduct`).

2. **Queries Return Data**: Queries retrieve data without modifying it. They're side-effect-free and can be cached or repeated safely.

3. **Separate Models**: The write model is optimized for enforcing business rules and maintaining consistency. The read model is optimized for query performance and UI needs.

4. **Eventual Consistency**: In full CQRS, the read model may be eventually consistent with the write model, updated via domain events.

5. **One-Way Data Flow**: Commands → Write Model → Events → Read Model → Queries

---

## CQRS Variants

### Simple CQRS (What We'll Implement)

Uses the same database but different code paths and models for reads and writes. The read model queries the same database but bypasses business logic layers.

```
Command → Command Handler → Domain Model → Database
                                              ↑
Query → Query Handler → Read Model ───────────┘
```

### Full CQRS with Event Sourcing

Uses separate databases for reads and writes, synchronized via events. This is more complex and typically not needed for most applications.

```
Command → Command Handler → Event Store → Events → Read Database
                                                         ↑
Query → Query Handler → Read Database ───────────────────┘
```

For this Shopping Cart project, we'll implement **Simple CQRS** with separate command and query models but a shared database.

---

## Target Package Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── command/                                  # WRITE SIDE
│   ├── order/
│   │   ├── CreateOrderCommand.java          # Command definition
│   │   ├── CreateOrderCommandHandler.java   # Handles command execution
│   │   ├── OrderItemCommand.java
│   │   └── ShippingAddressCommand.java
│   ├── product/
│   │   ├── UpdateProductStockCommand.java
│   │   └── UpdateProductStockCommandHandler.java
│   └── handler/
│       └── CommandHandler.java              # Handler interface
│
├── query/                                    # READ SIDE
│   ├── category/
│   │   ├── GetAllCategoriesQuery.java
│   │   ├── GetAllCategoriesQueryHandler.java
│   │   └── CategoryReadModel.java           # Optimized for reading
│   ├── product/
│   │   ├── SearchProductsQuery.java
│   │   ├── SearchProductsQueryHandler.java
│   │   ├── ProductReadModel.java
│   │   └── ProductPageReadModel.java
│   ├── order/
│   │   ├── GetOrderByIdQuery.java
│   │   ├── GetOrderByIdQueryHandler.java
│   │   ├── OrderReadModel.java
│   │   └── OrderItemReadModel.java
│   └── handler/
│       └── QueryHandler.java                # Handler interface
│
├── domain/                                   # DOMAIN MODEL (for writes)
│   ├── model/
│   │   ├── Category.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── repository/                          # Write repositories
│   │   ├── CategoryRepository.java
│   │   ├── ProductRepository.java
│   │   └── OrderRepository.java
│   ├── event/                               # Domain events
│   │   ├── DomainEvent.java
│   │   ├── OrderCreatedEvent.java
│   │   └── ProductStockUpdatedEvent.java
│   └── exception/
│       ├── InsufficientStockException.java
│       └── ProductNotActiveException.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/                          # JPA entities (shared)
│   │   │   ├── CategoryEntity.java
│   │   │   ├── ProductEntity.java
│   │   │   ├── OrderEntity.java
│   │   │   └── OrderItemEntity.java
│   │   ├── repository/
│   │   │   ├── write/                       # Write-optimized repositories
│   │   │   │   ├── CategoryJpaRepository.java
│   │   │   │   ├── ProductJpaRepository.java
│   │   │   │   └── OrderJpaRepository.java
│   │   │   └── read/                        # Read-optimized repositories
│   │   │       ├── CategoryReadRepository.java
│   │   │       ├── ProductReadRepository.java
│   │   │       └── OrderReadRepository.java
│   │   ├── adapter/
│   │   │   ├── CategoryRepositoryAdapter.java
│   │   │   ├── ProductRepositoryAdapter.java
│   │   │   └── OrderRepositoryAdapter.java
│   │   ├── mapper/
│   │   │   ├── CategoryMapper.java
│   │   │   ├── ProductMapper.java
│   │   │   └── OrderMapper.java
│   │   └── specification/
│   │       └── ProductSpecifications.java
│   ├── event/
│   │   └── SpringEventPublisher.java
│   └── config/
│       ├── OpenApiConfig.java
│       └── DataInitializer.java
│
├── api/                                      # HTTP LAYER
│   ├── controller/
│   │   ├── CategoryController.java
│   │   ├── ProductController.java
│   │   └── OrderController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── OrderCreateRequest.java
│   │   │   ├── OrderItemRequest.java
│   │   │   └── ShippingAddressRequest.java
│   │   └── response/
│   │       ├── CategoryResponse.java
│   │       ├── ProductResponse.java
│   │       ├── ProductPageResponse.java
│   │       ├── OrderResponse.java
│   │       └── OrderItemResponse.java
│   ├── mapper/
│   │   ├── CategoryApiMapper.java
│   │   ├── ProductApiMapper.java
│   │   └── OrderApiMapper.java
│   └── exception/
│       └── GlobalExceptionHandler.java
│
└── shared/
    ├── cqrs/
    │   ├── Command.java                     # Marker interface
    │   ├── Query.java                       # Marker interface
    │   ├── CommandBus.java                  # Command dispatcher
    │   └── QueryBus.java                    # Query dispatcher
    └── event/
        └── DomainEventPublisher.java
```

---

## Core CQRS Infrastructure

### Command and Query Interfaces

```java
// shared/cqrs/Command.java
// Marker interface for all commands
// Commands represent the intent to change state
public interface Command<R> {
    // R is the return type (usually just an ID or void wrapper)
}
```

```java
// shared/cqrs/Query.java
// Marker interface for all queries
// Queries represent requests for data without side effects
public interface Query<R> {
    // R is the return type (the data being requested)
}
```

### Command and Query Handlers

```java
// command/handler/CommandHandler.java
// Generic interface for command handlers
// Each command has exactly one handler
public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}
```

```java
// query/handler/QueryHandler.java
// Generic interface for query handlers
// Each query has exactly one handler
public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}
```

### Command and Query Buses

```java
// shared/cqrs/CommandBus.java
// Dispatches commands to their handlers
// This acts as a mediator between the API layer and command handlers
@Component
public class CommandBus {
    
    private final ApplicationContext applicationContext;

    public CommandBus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public <C extends Command<R>, R> R dispatch(C command) {
        // Find the handler for this command type
        // Convention: CommandName + Handler
        String handlerName = command.getClass().getSimpleName() + "Handler";
        String beanName = Character.toLowerCase(handlerName.charAt(0)) + handlerName.substring(1);
        
        CommandHandler<C, R> handler = (CommandHandler<C, R>) applicationContext.getBean(beanName);
        return handler.handle(command);
    }
}
```

```java
// shared/cqrs/QueryBus.java
// Dispatches queries to their handlers
@Component
public class QueryBus {
    
    private final ApplicationContext applicationContext;

    public QueryBus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Query<R>, R> R dispatch(Q query) {
        // Find the handler for this query type
        String handlerName = query.getClass().getSimpleName() + "Handler";
        String beanName = Character.toLowerCase(handlerName.charAt(0)) + handlerName.substring(1);
        
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) applicationContext.getBean(beanName);
        return handler.handle(query);
    }
}
```

---

## Commands (Write Side)

Commands represent intentions to change the system state. They should be named with imperative verbs and contain all data needed to execute the operation.

### Command Definitions

```java
// command/order/CreateOrderCommand.java
// Command to create a new order
// Contains all data needed to create the order
public record CreateOrderCommand(
    Long customerId,
    List<OrderItemCommand> items,
    ShippingAddressCommand shippingAddress
) implements Command<CreateOrderResult> {
    
    // Validation in constructor
    public CreateOrderCommand {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }
    }
}
```

```java
// command/order/OrderItemCommand.java
public record OrderItemCommand(
    Long productId,
    Integer quantity
) {
    public OrderItemCommand {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (quantity == null || quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99");
        }
    }
}
```

```java
// command/order/ShippingAddressCommand.java
public record ShippingAddressCommand(
    String street,
    String city,
    String state,
    String zipCode,
    String country
) {
    public ShippingAddressCommand {
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

```java
// command/order/CreateOrderResult.java
// Result returned from the CreateOrderCommand
// Contains minimal data - just what's needed to confirm success
public record CreateOrderResult(
    Long orderId,
    String status,
    BigDecimal total,
    OffsetDateTime createdAt
) {}
```

```java
// command/product/UpdateProductStockCommand.java
public record UpdateProductStockCommand(
    Long productId,
    Integer quantityChange  // Positive to add, negative to subtract
) implements Command<Void> {
    
    public UpdateProductStockCommand {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (quantityChange == null || quantityChange == 0) {
            throw new IllegalArgumentException("Quantity change must be non-zero");
        }
    }
}
```

### Command Handlers

```java
// command/order/CreateOrderCommandHandler.java
// Handler for CreateOrderCommand
// This is where the write-side business logic lives
@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, CreateOrderResult> {
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public CreateOrderResult handle(CreateOrderCommand command) {
        // 1. Load all products needed for the order
        List<Long> productIds = command.items().stream()
            .map(OrderItemCommand::productId)
            .toList();
        
        List<Product> products = productRepository.findAllByIds(productIds);
        
        // 2. Validate all products exist
        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException("One or more products not found");
        }

        // 3. Create order items and validate business rules
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (OrderItemCommand itemCmd : command.items()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(itemCmd.productId()))
                .findFirst()
                .orElseThrow();

            // Validate product is active
            if (!product.isActive()) {
                throw new ProductNotActiveException("Product is inactive: " + product.getId());
            }
            
            // Validate stock availability
            if (product.getStock() < itemCmd.quantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getId() + 
                    ". Available: " + product.getStock() + ", Requested: " + itemCmd.quantity()
                );
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemCmd.quantity()));
            subtotal = subtotal.add(lineSubtotal);

            OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(itemCmd.quantity())
                .unitPrice(unitPrice)
                .subtotal(lineSubtotal)
                .build();
            orderItems.add(orderItem);
        }

        // 4. Calculate totals
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        // 5. Create order aggregate
        Order order = Order.builder()
            .customerId(command.customerId())
            .status(OrderStatus.PENDING)
            .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
            .tax(tax)
            .total(total)
            .shippingStreet(command.shippingAddress().street())
            .shippingCity(command.shippingAddress().city())
            .shippingState(command.shippingAddress().state())
            .shippingZipCode(command.shippingAddress().zipCode())
            .shippingCountry(command.shippingAddress().country())
            .items(new ArrayList<>())
            .build();

        // 6. Link items to order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        // 7. Persist order
        Order savedOrder = orderRepository.save(order);

        // 8. Update product stock
        for (OrderItem item : savedOrder.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // 9. Publish domain event
        eventPublisher.publish(new OrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getCustomerId(),
            savedOrder.getTotal(),
            savedOrder.getCreatedAt()
        ));

        // 10. Return minimal result
        return new CreateOrderResult(
            savedOrder.getId(),
            savedOrder.getStatus().name(),
            savedOrder.getTotal(),
            savedOrder.getCreatedAt()
        );
    }
}
```

```java
// command/product/UpdateProductStockCommandHandler.java
@Component
@RequiredArgsConstructor
public class UpdateProductStockCommandHandler implements CommandHandler<UpdateProductStockCommand, Void> {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Void handle(UpdateProductStockCommand command) {
        Product product = productRepository.findById(command.productId())
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + command.productId()));

        int newStock = product.getStock() + command.quantityChange();
        
        if (newStock < 0) {
            throw new InsufficientStockException(
                "Cannot reduce stock below zero. Current: " + product.getStock() + 
                ", Change: " + command.quantityChange()
            );
        }

        product.setStock(newStock);
        productRepository.save(product);

        eventPublisher.publish(new ProductStockUpdatedEvent(
            product.getId(),
            product.getStock(),
            OffsetDateTime.now()
        ));

        return null;
    }
}
```

---

## Queries (Read Side)

Queries represent requests for data. They should be named with interrogative phrases and return data optimized for the client's needs.

### Read Models

Read models are optimized for query performance. They may be denormalized and don't contain business logic.

```java
// query/category/CategoryReadModel.java
// Optimized for reading - no behavior, just data
public record CategoryReadModel(
    Long id,
    String name,
    String description,
    boolean active,
    OffsetDateTime createdAt
) {}
```

```java
// query/product/ProductReadModel.java
// Denormalized with category information included
public record ProductReadModel(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Long categoryId,
    String categoryName,  // Denormalized for query efficiency
    Integer stock,
    String imageUrl,
    boolean active
) {}
```

```java
// query/product/ProductPageReadModel.java
// Page wrapper for product queries
public record ProductPageReadModel(
    List<ProductReadModel> content,
    long totalElements,
    int totalPages,
    int number,
    int size,
    boolean first,
    boolean last
) {}
```

```java
// query/order/OrderReadModel.java
public record OrderReadModel(
    Long orderId,
    String status,
    List<OrderItemReadModel> items,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total,
    OffsetDateTime createdAt
) {}
```

```java
// query/order/OrderItemReadModel.java
public record OrderItemReadModel(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
```

### Query Definitions

```java
// query/category/GetAllCategoriesQuery.java
// Query to retrieve all active categories
public record GetAllCategoriesQuery() implements Query<List<CategoryReadModel>> {
    // No parameters needed for this query
}
```

```java
// query/product/SearchProductsQuery.java
// Query with filtering, pagination, and sorting parameters
public record SearchProductsQuery(
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
) implements Query<ProductPageReadModel> {
    
    // Default values in compact constructor
    public SearchProductsQuery {
        if (active == null) active = true;
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null) sortDirection = "asc";
    }
    
    // Convenience factory for simple queries
    public static SearchProductsQuery all() {
        return new SearchProductsQuery(null, null, null, null, null, true, 0, 20, "id", "asc");
    }
    
    public static SearchProductsQuery byCategory(Long categoryId) {
        return new SearchProductsQuery(categoryId, null, null, null, null, true, 0, 20, "id", "asc");
    }
}
```

```java
// query/order/GetOrderByIdQuery.java
public record GetOrderByIdQuery(Long orderId) implements Query<OrderReadModel> {
    
    public GetOrderByIdQuery {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
    }
}
```

### Query Handlers

```java
// query/category/GetAllCategoriesQueryHandler.java
// Handles category listing query
// Uses read-optimized repository - bypasses domain model
@Component
@RequiredArgsConstructor
public class GetAllCategoriesQueryHandler implements QueryHandler<GetAllCategoriesQuery, List<CategoryReadModel>> {
    
    private final CategoryReadRepository categoryReadRepository;

    @Override
    @Transactional(readOnly = true)  // Read-only transaction for optimization
    public List<CategoryReadModel> handle(GetAllCategoriesQuery query) {
        // Direct projection query - no mapping through domain model
        return categoryReadRepository.findAllActiveCategories();
    }
}
```

```java
// query/product/SearchProductsQueryHandler.java
@Component
@RequiredArgsConstructor
public class SearchProductsQueryHandler implements QueryHandler<SearchProductsQuery, ProductPageReadModel> {
    
    private final ProductReadRepository productReadRepository;
    private final CategoryReadRepository categoryReadRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductPageReadModel handle(SearchProductsQuery query) {
        // Validate category exists if specified
        if (query.categoryId() != null && !categoryReadRepository.existsById(query.categoryId())) {
            throw new CategoryNotFoundException("Category not found: " + query.categoryId());
        }

        // Build page request
        Sort sort = Sort.by(Sort.Direction.fromString(query.sortDirection()), query.sortBy());
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        // Execute query using read-optimized repository
        Page<ProductReadModel> page = productReadRepository.searchProducts(
            query.categoryId(),
            query.name(),
            query.minPrice(),
            query.maxPrice(),
            query.inStock(),
            query.active(),
            pageable
        );

        return new ProductPageReadModel(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.isFirst(),
            page.isLast()
        );
    }
}
```

```java
// query/order/GetOrderByIdQueryHandler.java
@Component
@RequiredArgsConstructor
public class GetOrderByIdQueryHandler implements QueryHandler<GetOrderByIdQuery, OrderReadModel> {
    
    private final OrderReadRepository orderReadRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderReadModel handle(GetOrderByIdQuery query) {
        return orderReadRepository.findOrderById(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + query.orderId()));
    }
}
```

### Read Repositories

```java
// infrastructure/persistence/repository/read/CategoryReadRepository.java
// Read-optimized repository using projections
public interface CategoryReadRepository extends JpaRepository<CategoryEntity, Long> {
    
    // Direct projection to read model
    @Query("""
        SELECT new app.quantun.architecture.query.category.CategoryReadModel(
            c.id, c.name, c.description, c.active, c.createdAt
        )
        FROM CategoryEntity c
        WHERE c.active = true
        ORDER BY c.name
        """)
    List<CategoryReadModel> findAllActiveCategories();
}
```

```java
// infrastructure/persistence/repository/read/ProductReadRepository.java
public interface ProductReadRepository extends JpaRepository<ProductEntity, Long> {
    
    // Complex query with dynamic filtering using native query or specifications
    @Query("""
        SELECT new app.quantun.architecture.query.product.ProductReadModel(
            p.id, p.name, p.description, p.price,
            p.category.id, p.category.name,
            p.stock, p.imageUrl, p.active
        )
        FROM ProductEntity p
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
          AND (:inStock IS NULL OR :inStock = false OR p.stock > 0)
          AND (:active IS NULL OR p.active = :active)
        """)
    Page<ProductReadModel> searchProducts(
        @Param("categoryId") Long categoryId,
        @Param("name") String name,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("inStock") Boolean inStock,
        @Param("active") Boolean active,
        Pageable pageable
    );
}
```

```java
// infrastructure/persistence/repository/read/OrderReadRepository.java
public interface OrderReadRepository extends JpaRepository<OrderEntity, Long> {
    
    // Custom method returning Optional read model
    default Optional<OrderReadModel> findOrderById(Long orderId) {
        return findById(orderId).map(this::toReadModel);
    }
    
    private OrderReadModel toReadModel(OrderEntity entity) {
        List<OrderItemReadModel> items = entity.getItems().stream()
            .map(item -> new OrderItemReadModel(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
            ))
            .toList();
        
        return new OrderReadModel(
            entity.getId(),
            entity.getStatus().name(),
            items,
            entity.getSubtotal(),
            entity.getTax(),
            entity.getTotal(),
            entity.getCreatedAt()
        );
    }
}
```

---

## API Layer

The API layer uses the command and query buses to dispatch operations.

```java
// api/controller/CategoryController.java
@Tag(name = "Categories", description = "Product category management endpoints")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final QueryBus queryBus;
    private final CategoryApiMapper mapper;

    @Operation(summary = "Get all product categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        // Dispatch query through query bus
        List<CategoryReadModel> categories = queryBus.dispatch(new GetAllCategoriesQuery());
        
        // Map to API response
        List<CategoryResponse> response = categories.stream()
            .map(mapper::toResponse)
            .toList();
        
        return ResponseEntity.ok(response);
    }
}
```

```java
// api/controller/ProductController.java
@Tag(name = "Products", description = "Product catalog management endpoints")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final QueryBus queryBus;
    private final ProductApiMapper mapper;

    @Operation(summary = "Search and filter products")
    @GetMapping
    public ResponseEntity<ProductPageResponse> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @ParameterObject Pageable pageable
    ) {
        // Build query from request parameters
        SearchProductsQuery query = new SearchProductsQuery(
            categoryId,
            name,
            minPrice,
            maxPrice,
            inStock,
            active,
            pageable.getPageNumber(),
            pageable.getPageSize(),
            pageable.getSort().isSorted() 
                ? pageable.getSort().iterator().next().getProperty() 
                : "id",
            pageable.getSort().isSorted() 
                ? pageable.getSort().iterator().next().getDirection().name().toLowerCase() 
                : "asc"
        );
        
        // Dispatch query
        ProductPageReadModel result = queryBus.dispatch(query);
        
        // Map to API response
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
```

```java
// api/controller/OrderController.java
@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final OrderApiMapper mapper;

    @Operation(summary = "Create a new order")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        // Map request to command
        CreateOrderCommand command = mapper.toCommand(request);
        
        // Dispatch command
        CreateOrderResult result = commandBus.dispatch(command);
        
        // After command succeeds, query for full order details
        // This demonstrates CQRS: command returns minimal data, query returns full details
        OrderReadModel orderDetails = queryBus.dispatch(new GetOrderByIdQuery(result.orderId()));
        
        // Map to API response
        OrderResponse response = mapper.toResponse(orderDetails);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

## Domain Events

Domain events enable communication between the write and read sides (and other parts of the system).

```java
// domain/event/OrderCreatedEvent.java
public record OrderCreatedEvent(
    Long orderId,
    Long customerId,
    BigDecimal total,
    OffsetDateTime occurredOn
) implements DomainEvent {}
```

```java
// domain/event/ProductStockUpdatedEvent.java
public record ProductStockUpdatedEvent(
    Long productId,
    Integer newStock,
    OffsetDateTime occurredOn
) implements DomainEvent {}
```

```java
// infrastructure/event/SpringEventPublisher.java
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

```java
// Example event listener (could update a separate read database in full CQRS)
@Component
@Slf4j
public class OrderEventListener {
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: {} with total {}", event.orderId(), event.total());
        // In full CQRS, this could update a separate read database
        // For simple CQRS, might trigger cache invalidation, notifications, etc.
    }
}
```

---

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                API LAYER                                         │
│                                                                                  │
│   api/controller/                     api/dto/                                   │
│   - CategoryController                - request/                                 │
│   - ProductController                 - response/                                │
│   - OrderController                                                              │
│                                                                                  │
└───────────────────────────────┬───────────────────────────────────┬─────────────┘
                                │                                   │
                                ▼                                   ▼
┌───────────────────────────────────────────┐   ┌───────────────────────────────────┐
│            COMMAND SIDE (Write)           │   │           QUERY SIDE (Read)       │
│                                           │   │                                   │
│   shared/cqrs/                            │   │   shared/cqrs/                    │
│   - CommandBus                            │   │   - QueryBus                      │
│                                           │   │                                   │
│   command/                                │   │   query/                          │
│   ├── order/                              │   │   ├── category/                   │
│   │   ├── CreateOrderCommand             │   │   │   ├── GetAllCategoriesQuery   │
│   │   └── CreateOrderCommandHandler      │   │   │   ├── GetAllCategoriesHandler │
│   └── product/                            │   │   │   └── CategoryReadModel       │
│       ├── UpdateProductStockCommand      │   │   ├── product/                    │
│       └── UpdateProductStockHandler      │   │   │   ├── SearchProductsQuery     │
│                                           │   │   │   ├── SearchProductsHandler  │
│   Uses:                                   │   │   │   └── ProductReadModel       │
│   - Domain Model (write)                  │   │   └── order/                      │
│   - Write Repositories                    │   │       └── ...                     │
│   - Domain Events                         │   │                                   │
│                                           │   │   Uses:                           │
│                                           │   │   - Read Repositories (projections)│
└───────────────────────────────┬───────────┘   └───────────────────────────────────┘
                                │                                   │
                                ▼                                   │
┌───────────────────────────────────────────┐                      │
│               DOMAIN LAYER                │                      │
│                                           │                      │
│   domain/model/                           │                      │
│   - Category, Product, Order, OrderItem   │                      │
│                                           │                      │
│   domain/repository/ (interfaces)         │                      │
│   - CategoryRepository                    │                      │
│   - ProductRepository                     │                      │
│   - OrderRepository                       │                      │
│                                           │                      │
│   domain/event/                           │                      │
│   - OrderCreatedEvent                     │                      │
│   - ProductStockUpdatedEvent              │                      │
│                                           │                      │
└───────────────────────────────┬───────────┘                      │
                                │                                   │
                                ▼                                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          INFRASTRUCTURE LAYER                                    │
│                                                                                  │
│   persistence/entity/                   persistence/repository/                  │
│   - CategoryEntity                      - write/ (JpaRepository)                 │
│   - ProductEntity                       - read/ (projections, optimized queries) │
│   - OrderEntity                                                                  │
│   - OrderItemEntity                     persistence/adapter/                     │
│                                         - Repository implementations             │
│                                                                                  │
│   event/                                                                         │
│   - SpringEventPublisher                                                         │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │    Database           │
                    │   (PostgreSQL)        │
                    │                       │
                    │  [Shared in Simple    │
                    │   CQRS - separate in  │
                    │   Full CQRS]          │
                    └───────────────────────┘
```

---
## Migration Steps

### Step 1: Create CQRS Infrastructure
1. Create `shared/cqrs/` with `Command`, `Query`, `CommandHandler`, `QueryHandler` interfaces
2. Create `CommandBus` and `QueryBus` dispatcher components

### Step 2: Create Commands
1. Create `command/order/` with `CreateOrderCommand` and related DTOs
2. Create `command/product/` if needed for stock updates
3. Ensure commands contain validation logic

### Step 3: Create Command Handlers
1. Create `CreateOrderCommandHandler` with write-side business logic
2. Move transaction boundaries to command handlers
3. Add domain event publishing

### Step 4: Create Read Models
1. Create `query/*/` packages with read models (records)
2. Design read models optimized for UI needs (denormalized)

### Step 5: Create Queries
1. Create query definitions as records implementing `Query<R>`
2. Include filtering and pagination parameters

### Step 6: Create Query Handlers
1. Create query handlers using read-optimized repositories
2. Use `@Transactional(readOnly = true)` for optimization

### Step 7: Create Read Repositories
1. Create `infrastructure/persistence/repository/read/` with projection queries
2. Use JPQL constructor expressions for direct read model creation

### Step 8: Update API Layer
1. Update controllers to use `CommandBus` and `QueryBus`
2. Map requests to commands/queries, responses from read models

### Step 9: Update Infrastructure
1. Ensure domain events are published using Spring's event mechanism
2. Add event listeners if needed
3. Update `DataInitializer`

---

## Benefits of CQRS

1. **Optimized Models**: Read and write models can be independently optimized.
2. **Scalability**: Read and write sides can be scaled independently.
3. **Simpler Queries**: Read models can be denormalized for simpler, faster queries.
4. **Clear Intent**: Commands and queries express intent clearly.
5. **Testability**: Command and query handlers are easy to unit test.
6. **Event Integration**: Natural fit for event-driven architectures.

---

## Notes for AI Agent

When refactoring to CQRS:

1. **Preserve All API Contracts**: HTTP interfaces remain identical.
2. **Commands Modify State**: No queries should have side effects.
3. **Queries Return Data**: No commands should return data (except IDs/status).
4. **Read Models Are Records**: Immutable, optimized for query needs.
5. **Command Handlers Are Transactional**: They own the transaction boundary.
6. **Query Handlers Are Read-Only**: Use `@Transactional(readOnly = true)`.
7. **Use Projections**: Read repositories should project directly to read models.
8. **Domain Events**: Publish events from command handlers using Spring events.
9. **Buses Dispatch Operations**: Controllers should use buses, not direct handler calls.
10. **DataInitializer**: May need to dispatch commands or use write repositories directly.