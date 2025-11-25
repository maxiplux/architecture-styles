# Modular Monolith Architecture

## Overview

A Modular Monolith is a single deployable unit (monolith) that is internally structured into well-defined, loosely-coupled modules. Each module has clear boundaries, its own internal structure, and communicates with other modules through explicit public APIs (facades). This architecture provides many benefits of microservices (modularity, clear boundaries, team autonomy) while avoiding the operational complexity of distributed systems.

The key insight is that the problems people associate with monoliths (tight coupling, spaghetti code, difficulty scaling teams) aren't inherent to monoliths—they're symptoms of poor modularization. A well-structured monolith can be just as maintainable as microservices, and it's often the right stepping stone before (or instead of) going to microservices.

---

## Core Principles

1. **Strong Module Boundaries**: Each module has a clear boundary. External code can only access a module through its public API (facade).

2. **Internal Implementation Hiding**: Each module's internal structure (entities, repositories, services) is hidden from other modules.

3. **Explicit Dependencies**: Module dependencies are explicit and ideally one-directional. Circular dependencies are forbidden.

4. **Shared Kernel**: Truly shared concepts (like base classes, common value objects) live in a shared kernel that all modules can depend on.

5. **Module Communication**: Modules communicate through facades (synchronous) or events (asynchronous), never by directly accessing each other's internals.

6. **Single Deployment Unit**: Despite the internal structure, the entire application deploys as one unit.

---

## Module Structure

Each module follows a consistent internal structure:

```
module/
├── api/                    # PUBLIC API (what other modules can see)
│   ├── ModuleFacade.java  # Entry point for other modules
│   ├── dto/               # DTOs exposed to other modules
│   └── events/            # Events published by this module
│
├── internal/              # PRIVATE IMPLEMENTATION (hidden from other modules)
│   ├── domain/           # Domain entities
│   ├── repository/       # Data access
│   ├── service/          # Business logic
│   └── ...
│
└── web/                   # HTTP ENDPOINTS (if this module exposes REST APIs)
    ├── controller/
    └── dto/
```

---

## Target Package Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── modules/
│   ├── catalog/                              # CATALOG MODULE
│   │   ├── api/                             # Public API
│   │   │   ├── CatalogFacade.java          # Entry point for other modules
│   │   │   ├── dto/
│   │   │   │   ├── CategoryDto.java
│   │   │   │   ├── ProductDto.java
│   │   │   │   └── ProductSearchCriteria.java
│   │   │   └── events/
│   │   │       └── ProductStockChangedEvent.java
│   │   │
│   │   ├── internal/                        # Private implementation
│   │   │   ├── domain/
│   │   │   │   ├── Category.java
│   │   │   │   └── Product.java
│   │   │   ├── repository/
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   └── ProductRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CategoryService.java
│   │   │   │   └── ProductService.java
│   │   │   ├── specification/
│   │   │   │   └── ProductSpecifications.java
│   │   │   └── mapper/
│   │   │       ├── CategoryMapper.java
│   │   │       └── ProductMapper.java
│   │   │
│   │   └── web/                             # REST API
│   │       ├── controller/
│   │       │   ├── CategoryController.java
│   │       │   └── ProductController.java
│   │       └── dto/
│   │           ├── CategoryResponse.java
│   │           ├── ProductResponse.java
│   │           └── ProductPageResponse.java
│   │
│   └── order/                                # ORDER MODULE
│       ├── api/                             # Public API
│       │   ├── OrderFacade.java            # Entry point for other modules
│       │   ├── dto/
│       │   │   ├── OrderDto.java
│       │   │   └── OrderItemDto.java
│       │   └── events/
│       │       ├── OrderCreatedEvent.java
│       │       └── OrderCancelledEvent.java
│       │
│       ├── internal/                        # Private implementation
│       │   ├── domain/
│       │   │   ├── Order.java
│       │   │   ├── OrderItem.java
│       │   │   └── OrderStatus.java
│       │   ├── repository/
│       │   │   └── OrderRepository.java
│       │   ├── service/
│       │   │   └── OrderService.java
│       │   └── mapper/
│       │       └── OrderMapper.java
│       │
│       └── web/                             # REST API
│           ├── controller/
│           │   └── OrderController.java
│           └── dto/
│               ├── OrderCreateRequest.java
│               ├── OrderItemRequest.java
│               ├── ShippingAddressRequest.java
│               └── OrderResponse.java
│
└── shared/                                   # SHARED KERNEL
    ├── domain/
    │   └── Money.java                       # Shared value object
    ├── event/
    │   ├── DomainEvent.java                # Event marker interface
    │   └── DomainEventPublisher.java       # Event publishing abstraction
    ├── exception/
    │   ├── NotFoundException.java
    │   ├── BadRequestException.java
    │   └── ConflictException.java
    └── config/
        ├── OpenApiConfig.java
        ├── EventConfig.java
        ├── ModuleConfig.java               # Module wiring configuration
        └── DataInitializer.java
```

---

## Module Implementation

### Catalog Module

#### Public API (Facade)

The facade is the **only** public class that other modules should use. It defines what operations the catalog module supports.

```java
// modules/catalog/api/CatalogFacade.java
// This is the ONLY class other modules should import from the catalog module
@Component
@RequiredArgsConstructor
public class CatalogFacade {
    
    private final CategoryService categoryService;
    private final ProductService productService;
    
    // === Category Operations ===
    
    public List<CategoryDto> getAllActiveCategories() {
        return categoryService.findAllActive();
    }
    
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryService.findById(id);
    }
    
    public boolean categoryExists(Long id) {
        return categoryService.existsById(id);
    }
    
    // === Product Operations ===
    
    public Page<ProductDto> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        return productService.search(criteria, pageable);
    }
    
    public Optional<ProductDto> getProductById(Long id) {
        return productService.findById(id);
    }
    
    public List<ProductDto> getProductsByIds(List<Long> ids) {
        return productService.findAllByIds(ids);
    }
    
    /**
     * Decreases stock for a product. Used by the order module when creating orders.
     * Publishes ProductStockChangedEvent on success.
     * 
     * @param productId the product ID
     * @param quantity the quantity to decrease
     * @throws NotFoundException if product doesn't exist
     * @throws ConflictException if insufficient stock
     */
    public void decreaseProductStock(Long productId, int quantity) {
        productService.decreaseStock(productId, quantity);
    }
    
    /**
     * Increases stock for a product. Used when orders are cancelled.
     * 
     * @param productId the product ID  
     * @param quantity the quantity to increase
     */
    public void increaseProductStock(Long productId, int quantity) {
        productService.increaseStock(productId, quantity);
    }
    
    /**
     * Validates that all products exist and have sufficient stock.
     * 
     * @param productQuantities map of productId to requested quantity
     * @throws BadRequestException if any product doesn't exist, is inactive, or has insufficient stock
     */
    public void validateProductAvailability(Map<Long, Integer> productQuantities) {
        productService.validateAvailability(productQuantities);
    }
}
```

#### Public DTOs

DTOs in the `api/dto/` package are the data contracts that other modules use.

```java
// modules/catalog/api/dto/CategoryDto.java
// Immutable DTO for cross-module communication
public record CategoryDto(
    Long id,
    String name,
    String description,
    boolean active
) {}
```

```java
// modules/catalog/api/dto/ProductDto.java
public record ProductDto(
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
// modules/catalog/api/dto/ProductSearchCriteria.java
public record ProductSearchCriteria(
    Long categoryId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean inStock,
    Boolean active
) {
    // Default active to true if not specified
    public ProductSearchCriteria {
        if (active == null) {
            active = true;
        }
    }
}
```

#### Public Events

Events that other modules might want to listen to.

```java
// modules/catalog/api/events/ProductStockChangedEvent.java
public record ProductStockChangedEvent(
    Long productId,
    String productName,
    Integer previousStock,
    Integer newStock,
    Integer changeAmount,
    OffsetDateTime occurredOn
) implements DomainEvent {}
```

#### Internal Domain (Hidden)

The internal domain is not accessible from outside the module. Notice these classes are in the `internal` package.

```java
// modules/catalog/internal/domain/Category.java
// Package-private or module-internal - NOT accessible from other modules
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
```

```java
// modules/catalog/internal/domain/Product.java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Product {
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

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    // Business methods
    boolean hasStock(int quantity) {
        return stock != null && stock >= quantity;
    }
    
    void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stock -= quantity;
    }
    
    void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
```

#### Internal Repository (Hidden)

```java
// modules/catalog/internal/repository/CategoryRepository.java
// Package-private - only accessible within the catalog module
interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrue();
}
```

```java
// modules/catalog/internal/repository/ProductRepository.java
interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
```

#### Internal Service (Hidden)

```java
// modules/catalog/internal/service/ProductService.java
@Service
@RequiredArgsConstructor
class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;
    private final DomainEventPublisher eventPublisher;

    public Page<ProductDto> search(ProductSearchCriteria criteria, Pageable pageable) {
        // Validate category exists if specified
        if (criteria.categoryId() != null && !categoryRepository.existsById(criteria.categoryId())) {
            throw new NotFoundException("Category not found: " + criteria.categoryId());
        }

        Specification<Product> spec = ProductSpecifications.fromCriteria(criteria);
        Page<Product> products = productRepository.findAll(spec, pageable);
        
        return products.map(mapper::toDto);
    }

    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id).map(mapper::toDto);
    }

    public List<ProductDto> findAllByIds(List<Long> ids) {
        return productRepository.findAllById(ids).stream()
            .map(mapper::toDto)
            .toList();
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        
        int previousStock = product.getStock();
        
        if (!product.hasStock(quantity)) {
            throw new ConflictException(
                "Insufficient stock for product " + productId + 
                ". Available: " + product.getStock() + ", Requested: " + quantity
            );
        }
        
        product.decreaseStock(quantity);
        productRepository.save(product);
        
        // Publish event for other modules to react
        eventPublisher.publish(new ProductStockChangedEvent(
            product.getId(),
            product.getName(),
            previousStock,
            product.getStock(),
            -quantity,
            OffsetDateTime.now()
        ));
    }

    @Transactional
    public void increaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        
        int previousStock = product.getStock();
        product.increaseStock(quantity);
        productRepository.save(product);
        
        eventPublisher.publish(new ProductStockChangedEvent(
            product.getId(),
            product.getName(),
            previousStock,
            product.getStock(),
            quantity,
            OffsetDateTime.now()
        ));
    }

    public void validateAvailability(Map<Long, Integer> productQuantities) {
        List<String> errors = new ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            Optional<Product> productOpt = productRepository.findById(productId);
            
            if (productOpt.isEmpty()) {
                errors.add("Product not found: " + productId);
                continue;
            }
            
            Product product = productOpt.get();
            
            if (!product.isActive()) {
                errors.add("Product is inactive: " + productId);
            } else if (!product.hasStock(quantity)) {
                errors.add("Insufficient stock for product " + productId + 
                          ". Available: " + product.getStock() + ", Requested: " + quantity);
            }
        }
        
        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("; ", errors));
        }
    }
}
```

#### Internal Mapper (Hidden)

```java
// modules/catalog/internal/mapper/ProductMapper.java
@Component
class ProductMapper {
    
    public ProductDto toDto(Product product) {
        return new ProductDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory().getId(),
            product.getCategory().getName(),
            product.getStock(),
            product.getImageUrl(),
            product.isActive()
        );
    }
}
```

#### Web Layer

The web layer exposes REST endpoints. It uses internal services but could also use the facade.

```java
// modules/catalog/web/controller/ProductController.java
@Tag(name = "Products", description = "Product catalog management endpoints")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final CatalogFacade catalogFacade;
    private final ProductWebMapper mapper;

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
        ProductSearchCriteria criteria = new ProductSearchCriteria(
            categoryId, name, minPrice, maxPrice, inStock, active
        );
        
        Page<ProductDto> products = catalogFacade.searchProducts(criteria, pageable);
        ProductPageResponse response = mapper.toPageResponse(products);
        
        return ResponseEntity.ok(response);
    }
}
```

### Order Module

#### Public API (Facade)

```java
// modules/order/api/OrderFacade.java
@Component
@RequiredArgsConstructor
public class OrderFacade {
    
    private final OrderService orderService;
    
    /**
     * Creates a new order.
     * 
     * @param customerId the customer placing the order
     * @param items list of product IDs and quantities
     * @param shippingAddress shipping details
     * @return the created order
     */
    public OrderDto createOrder(Long customerId, List<OrderItemDto> items, 
                                 ShippingAddressDto shippingAddress) {
        return orderService.createOrder(customerId, items, shippingAddress);
    }
    
    /**
     * Gets an order by ID.
     */
    public Optional<OrderDto> getOrderById(Long orderId) {
        return orderService.findById(orderId);
    }
    
    /**
     * Gets all orders for a customer.
     */
    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        return orderService.findByCustomerId(customerId);
    }
    
    /**
     * Cancels an order. Returns stock to products.
     */
    public void cancelOrder(Long orderId) {
        orderService.cancelOrder(orderId);
    }
}
```

#### Public DTOs

```java
// modules/order/api/dto/OrderDto.java
public record OrderDto(
    Long id,
    Long customerId,
    String status,
    List<OrderItemDto> items,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total,
    OffsetDateTime createdAt
) {}
```

```java
// modules/order/api/dto/OrderItemDto.java
public record OrderItemDto(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
```

```java
// modules/order/api/dto/ShippingAddressDto.java
public record ShippingAddressDto(
    String street,
    String city,
    String state,
    String zipCode,
    String country
) {}
```

#### Public Events

```java
// modules/order/api/events/OrderCreatedEvent.java
public record OrderCreatedEvent(
    Long orderId,
    Long customerId,
    BigDecimal total,
    List<OrderItemInfo> items,
    OffsetDateTime occurredOn
) implements DomainEvent {
    
    public record OrderItemInfo(Long productId, Integer quantity) {}
}
```

```java
// modules/order/api/events/OrderCancelledEvent.java
public record OrderCancelledEvent(
    Long orderId,
    Long customerId,
    List<OrderItemInfo> items,
    OffsetDateTime occurredOn
) implements DomainEvent {
    
    public record OrderItemInfo(Long productId, Integer quantity) {}
}
```

#### Internal Service (Hidden)

```java
// modules/order/internal/service/OrderService.java
@Service
@RequiredArgsConstructor
class OrderService {
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    
    private final OrderRepository orderRepository;
    private final CatalogFacade catalogFacade;  // Use the catalog module's facade
    private final OrderMapper mapper;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public OrderDto createOrder(Long customerId, List<OrderItemDto> items, 
                                 ShippingAddressDto shippingAddress) {
        // 1. Prepare product quantities map for validation
        Map<Long, Integer> productQuantities = items.stream()
            .collect(Collectors.toMap(OrderItemDto::productId, OrderItemDto::quantity));
        
        // 2. Validate all products are available (using catalog facade)
        catalogFacade.validateProductAvailability(productQuantities);
        
        // 3. Get product details from catalog module
        List<Long> productIds = items.stream().map(OrderItemDto::productId).toList();
        List<ProductDto> products = catalogFacade.getProductsByIds(productIds);
        
        // 4. Build order items with current prices
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemDto itemDto : items) {
            ProductDto product = products.stream()
                .filter(p -> p.id().equals(itemDto.productId()))
                .findFirst()
                .orElseThrow();
            
            BigDecimal lineSubtotal = product.price()
                .multiply(BigDecimal.valueOf(itemDto.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
            
            subtotal = subtotal.add(lineSubtotal);
            
            OrderItem orderItem = OrderItem.builder()
                .productId(product.id())
                .productName(product.name())
                .quantity(itemDto.quantity())
                .unitPrice(product.price())
                .subtotal(lineSubtotal)
                .build();
            orderItems.add(orderItem);
        }
        
        // 5. Calculate totals
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        
        // 6. Create and save order
        Order order = Order.builder()
            .customerId(customerId)
            .status(OrderStatus.PENDING)
            .subtotal(subtotal)
            .tax(tax)
            .total(total)
            .shippingStreet(shippingAddress.street())
            .shippingCity(shippingAddress.city())
            .shippingState(shippingAddress.state())
            .shippingZipCode(shippingAddress.zipCode())
            .shippingCountry(shippingAddress.country())
            .items(new ArrayList<>())
            .build();
        
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // 7. Decrease stock for each product (using catalog facade)
        for (OrderItem item : savedOrder.getItems()) {
            catalogFacade.decreaseProductStock(item.getProductId(), item.getQuantity());
        }
        
        // 8. Publish event
        List<OrderCreatedEvent.OrderItemInfo> eventItems = savedOrder.getItems().stream()
            .map(item -> new OrderCreatedEvent.OrderItemInfo(item.getProductId(), item.getQuantity()))
            .toList();
        
        eventPublisher.publish(new OrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getCustomerId(),
            savedOrder.getTotal(),
            eventItems,
            OffsetDateTime.now()
        ));
        
        return mapper.toDto(savedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        
        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED) {
            throw new ConflictException("Cannot cancel shipped or delivered orders");
        }
        
        // Return stock to products
        for (OrderItem item : order.getItems()) {
            catalogFacade.increaseProductStock(item.getProductId(), item.getQuantity());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        // Publish cancellation event
        List<OrderCancelledEvent.OrderItemInfo> eventItems = order.getItems().stream()
            .map(item -> new OrderCancelledEvent.OrderItemInfo(item.getProductId(), item.getQuantity()))
            .toList();
        
        eventPublisher.publish(new OrderCancelledEvent(
            order.getId(),
            order.getCustomerId(),
            eventItems,
            OffsetDateTime.now()
        ));
    }

    public Optional<OrderDto> findById(Long id) {
        return orderRepository.findById(id).map(mapper::toDto);
    }

    public List<OrderDto> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(mapper::toDto)
            .toList();
    }
}
```

#### Internal Domain (Hidden)

```java
// modules/order/internal/domain/Order.java
@Entity
@Table(name = "customer_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "shipping_street", nullable = false, length = 200)
    private String shippingStreet;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", length = 100)
    private String shippingState;

    @Column(name = "shipping_zip_code", nullable = false, length = 20)
    private String shippingZipCode;

    @Column(name = "shipping_country", nullable = false, length = 100)
    private String shippingCountry;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
```

```java
// modules/order/internal/domain/OrderItem.java
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;  // Reference by ID, not entity - cross-module!

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
```

**Important Note**: The `OrderItem` references `productId` as a primitive `Long`, NOT as a JPA relationship to `Product`. This is intentional—modules should not share JPA entity relationships across boundaries. Cross-module references are by ID only.

#### Web Layer

```java
// modules/order/web/controller/OrderController.java
@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderFacade orderFacade;
    private final OrderWebMapper mapper;

    @Operation(summary = "Create a new order")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        // Map web request to facade DTOs
        List<OrderItemDto> items = request.items().stream()
            .map(item -> new OrderItemDto(
                item.productId(), 
                null,  // productName will be filled by service
                item.quantity(), 
                null,  // unitPrice will be filled by service
                null   // subtotal will be filled by service
            ))
            .toList();
        
        ShippingAddressDto shippingAddress = new ShippingAddressDto(
            request.shippingAddress().street(),
            request.shippingAddress().city(),
            request.shippingAddress().state(),
            request.shippingAddress().zipCode(),
            request.shippingAddress().country()
        );
        
        OrderDto order = orderFacade.createOrder(request.customerId(), items, shippingAddress);
        OrderResponse response = mapper.toResponse(order);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

## Shared Kernel

The shared kernel contains code that all modules can depend on.

```java
// shared/event/DomainEvent.java
// Marker interface for all domain events
public interface DomainEvent {
    OffsetDateTime occurredOn();
}
```

```java
// shared/event/DomainEventPublisher.java
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
```

```java
// shared/config/EventConfig.java
@Configuration
public class EventConfig {
    
    @Bean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher publisher) {
        return publisher::publishEvent;
    }
}
```

---

## Enforcing Module Boundaries

### Using ArchUnit

Add ArchUnit tests to enforce module boundaries at build time:

```java
// src/test/java/app/quantun/architecture/ModuleBoundaryTest.java
@AnalyzeClasses(packages = "app.quantun.architecture")
public class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule catalog_internal_not_accessed_from_outside =
        noClasses()
            .that().resideOutsideOfPackage("..modules.catalog..")
            .should().accessClassesThat()
            .resideInAPackage("..modules.catalog.internal..");

    @ArchTest
    static final ArchRule order_internal_not_accessed_from_outside =
        noClasses()
            .that().resideOutsideOfPackage("..modules.order..")
            .should().accessClassesThat()
            .resideInAPackage("..modules.order.internal..");

    @ArchTest
    static final ArchRule modules_should_only_access_api_of_other_modules =
        classes()
            .that().resideInAPackage("..modules.order..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                "..modules.order..",           // Own module
                "..modules.catalog.api..",     // Catalog's public API
                "..shared..",                  // Shared kernel
                "java..",                      // Java standard library
                "jakarta..",                   // Jakarta EE
                "org.springframework..",       // Spring
                "lombok.."                     // Lombok
            );

    @ArchTest
    static final ArchRule no_circular_module_dependencies =
        slices()
            .matching("app.quantun.architecture.modules.(*)..")
            .should().beFreeOfCycles();
}
```

### Using Spring Modulith

Alternatively, use Spring Modulith for automatic module detection and verification:

```java
// src/test/java/app/quantun/architecture/ModularityTest.java
@SpringBootTest
public class ModularityTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void verifyModularity() {
        ApplicationModules modules = ApplicationModules.of(ArchitectureApplication.class);
        modules.verify();
    }

    @Test
    void documentModules() {
        ApplicationModules modules = ApplicationModules.of(ArchitectureApplication.class);
        new Documenter(modules)
            .writeDocumentation()
            .writeModulesAsPlantUml();
    }
}
```

---

## MapStruct Integration

In a Modular Monolith, MapStruct plays a key role in maintaining module boundaries. Each module's `internal` package has its own domain entities, while the `api` package exposes stable, public DTOs. MapStruct is used within each module to handle the transformation between these internal entities and the public DTOs, ensuring that other modules never need to know about the internal implementation details.

### Adding MapStruct Dependency

```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

### Module-Internal Mappers

Each module that needs to expose data through its facade will have its own mappers. These mappers are part of the module's `internal` implementation.

```java
// modules/catalog/internal/mapper/ProductMapper.java
@Mapper(componentModel = "spring")
interface ProductMapper {
    
    // Internal entity -> Public DTO
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDto toDto(Product product);
    
    List<ProductDto> toDtoList(List<Product> products);
}
```

```java
// modules/order/internal/mapper/OrderMapper.java
@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
interface OrderMapper {

    // Internal entity -> Public DTO
    @Mapping(source = "id", target = "id")
    @Mapping(source = "customerId", target = "customerId")
    OrderDto toDto(Order order);
}

@Mapper(componentModel = "spring")
interface OrderItemMapper {
    
    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    OrderItemDto toDto(OrderItem item);
}
```

### Using Mappers in Services

The module's internal services use these mappers to prepare data for the public facade.

```java
// modules/catalog/internal/service/ProductService.java
@Service
@RequiredArgsConstructor
class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper mapper; // Injected mapper
    
    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id).map(mapper::toDto);
    }
    
    public Page<ProductDto> search(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecifications.fromCriteria(criteria);
        return productRepository.findAll(spec, pageable).map(mapper::toDto);
    }
}
```

---

## Testing Strategy

The primary goal of testing in a Modular Monolith is to verify each module's correctness in isolation and ensure that modules interact correctly through their public APIs.

### Test Dependencies

```gradle
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'
}
```

### Module Integration Tests

The most valuable tests are module-level integration tests. Each module is tested independently by loading only its own configuration and mocking its dependencies on other modules.

```java
// In test/java/app/quantun/architecture/modules/order/
@SpringBootTest(classes = OrderModuleTestConfig.class)
@Testcontainers
class OrderModuleIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        // ...
    }

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private CatalogFacade catalogFacade; // Mock the dependency on the other module

    @Test
    void createOrder_whenCatalogFacadeSucceeds_createsOrder() {
        // Given - mock the behavior of the external catalog module
        ProductDto productDto = new ProductDto(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 1L, "Elec", 10, "img", true);
        when(catalogFacade.getProductsByIds(List.of(1L))).thenReturn(List.of(productDto));
        doNothing().when(catalogFacade).validateProductAvailability(anyMap());
        doNothing().when(catalogFacade).decreaseProductStock(1L, 2);

        List<OrderItemDto> items = List.of(new OrderItemDto(1L, "Laptop", 2, BigDecimal.valueOf(1000), BigDecimal.valueOf(2000)));
        ShippingAddressDto shipping = new ShippingAddressDto("street", "city", "st", "zip", "USA");

        // When
        OrderDto createdOrder = orderFacade.createOrder(1L, items, shipping);

        // Then
        assertNotNull(createdOrder.id());
        assertEquals("PENDING", createdOrder.status());
        
        // Verify order was saved to the database
        assertTrue(orderRepository.existsById(createdOrder.id()));
        
        // Verify the module called its dependency correctly
        verify(catalogFacade).validateProductAvailability(anyMap());
        verify(catalogFacade).decreaseProductStock(1L, 2);
    }
}
```

This test loads only the `Order` module's beans and mocks the `CatalogFacade`, ensuring the `Order` module can be tested in complete isolation.

### Architecture Tests (Boundary Enforcement)

ArchUnit tests are critical for enforcing module boundaries.

```java
// In src/test/java/app/quantun/architecture/
@AnalyzeClasses(packages = "app.quantun.architecture.modules")
class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule order_module_should_only_depend_on_catalog_api =
        classes()
            .that().resideInAPackage("..order..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                "..order..",
                "..catalog.api..", // <-- Allowed dependency
                "..shared..",
                "java..", "org.springframework.." // etc.
            );

    @ArchTest
    static final ArchRule internal_packages_should_not_be_accessed =
        noClasses()
            .that().resideOutsideOfPackage("..modules.(*)..")
            .should().accessClassesThat()
            .resideInAPackage("..modules.(*).internal..");

    @ArchTest
    static final ArchRule modules_should_be_free_of_cycles =
        slices().matching("..modules.(*)..").should().beFreeOfCycles();
}
```

---

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                 MODULES                                          │
│                                                                                  │
│  ┌────────────────────────────────────┐    ┌────────────────────────────────────┐│
│  │         CATALOG MODULE             │    │          ORDER MODULE              ││
│  │                                    │    │                                    ││
│  │  ┌──────────────────────────────┐  │    │  ┌──────────────────────────────┐  ││
│  │  │           api/               │  │    │  │           api/               │  ││
│  │  │  - CatalogFacade ◄───────────┼──┼────┼──│  - OrderFacade               │  ││
│  │  │  - CategoryDto               │  │    │  │  - OrderDto                  │  ││
│  │  │  - ProductDto                │  │    │  │  - OrderCreatedEvent         │  ││
│  │  │  - ProductStockChangedEvent  │  │    │  │                              │  ││
│  │  └──────────────────────────────┘  │    │  └──────────────────────────────┘  ││
│  │               │                    │    │               │                    ││
│  │               │ uses               │    │               │ uses               ││
│  │               ▼                    │    │               ▼                    ││
│  │  ┌──────────────────────────────┐  │    │  ┌──────────────────────────────┐  ││
│  │  │        internal/             │  │    │  │        internal/             │  ││
│  │  │  - Category (entity)         │  │    │  │  - Order (entity)            │  ││
│  │  │  - Product (entity)          │  │    │  │  - OrderItem (entity)        │  ││
│  │  │  - CategoryRepository        │  │    │  │  - OrderRepository           │  ││
│  │  │  - ProductRepository         │  │    │  │  - OrderService              │  ││
│  │  │  - ProductService            │  │    │  │  - OrderMapper               │  ││
│  │  │  - CategoryService           │  │    │  │                              │  ││
│  │  │  - ProductSpecifications     │  │    │  └──────────────────────────────┘  ││
│  │  └──────────────────────────────┘  │    │                                    ││
│  │                                    │    │                                    ││
│  │  ┌──────────────────────────────┐  │    │  ┌──────────────────────────────┐  ││
│  │  │           web/               │  │    │  │           web/               │  ││
│  │  │  - CategoryController        │  │    │  │  - OrderController           │  ││
│  │  │  - ProductController         │  │    │  │  - Request/Response DTOs     │  ││
│  │  │  - Response DTOs             │  │    │  │                              │  ││
│  │  └──────────────────────────────┘  │    │  └──────────────────────────────┘  ││
│  │                                    │    │                                    ││
│  └────────────────────────────────────┘    └────────────────────────────────────┘│
│                                                                                  │
└────────────────────────────────────────────┬─────────────────────────────────────┘
                                             │
                                             │ depends on
                                             ▼
                              ┌───────────────────────────────────┐
                              │          SHARED KERNEL            │
                              │                                   │
                              │  - DomainEvent                    │
                              │  - DomainEventPublisher           │
                              │  - Exception classes              │
                              │  - Money (shared value object)    │
                              │  - Configuration                  │
                              │                                   │
                              └───────────────────────────────────┘
```

---

## Migration Steps

### Step 1: Create Module Structure

Create the directory structure for both modules:
```
modules/catalog/api/, internal/, web/
modules/order/api/, internal/, web/
shared/
```

### Step 2: Create Shared Kernel

1. Create `shared/event/DomainEvent.java`
2. Create `shared/event/DomainEventPublisher.java`
3. Move exception classes to `shared/exception/`
4. Create configuration classes in `shared/config/`

### Step 3: Build Catalog Module

1. Create `CatalogFacade` in `api/`
2. Create DTOs in `api/dto/`
3. Move `Category`, `Product` entities to `internal/domain/`
4. Move repositories to `internal/repository/`
5. Create services in `internal/service/`
6. Create controllers in `web/controller/`

### Step 4: Build Order Module

1. Create `OrderFacade` in `api/`
2. Create DTOs in `api/dto/`
3. Move `Order`, `OrderItem` entities to `internal/domain/`
4. **Change OrderItem to reference productId as Long, not Product entity**
5. Move repository to `internal/repository/`
6. Create OrderService that uses `CatalogFacade`
7. Create controller in `web/controller/`

### Step 5: Update Data Initializer

Update `DataInitializer` to use facades or internal services appropriately.

### Step 6: Add Boundary Tests

Add ArchUnit or Spring Modulith tests to enforce boundaries.

---

## Benefits of Vertical Slice Architecture

1. **Feature Isolation**: Changes to one feature don't affect others.
2. **Easier Navigation**: All code for a feature is in one place.
3. **Reduced Merge Conflicts**: Teams working on different features rarely touch the same files.
4. **Right-Sized Abstractions**: No over-engineering with generic interfaces.
5. **Simpler Mental Model**: Think in features, not layers.
6. **Easy to Delete**: Removing a feature means deleting one folder.

---

## Notes for AI Agent

When refactoring to Modular Monolith:

1. **Preserve All API Contracts**: HTTP interfaces remain identical.
2. **Facades Are Public**: Only facade and api/dto classes are accessible outside module.
3. **Internal Is Private**: Classes in internal/ package should be package-private.
4. **No Cross-Module JPA Relations**: Use IDs (Long) instead of entity references.
5. **Communication Via Facades**: Modules call each other through facades, never internals.
6. **Events Are Public**: Event classes go in api/events/ for cross-module consumption.
7. **Shared Kernel Is Minimal**: Only truly shared concepts go in shared/.
8. **OrderItem Stores ProductId**: Not a relationship to Product entity.
9. **Each Module Has Its Own Web Layer**: Controllers live within their module.
10. **DataInitializer**: Should use the catalog module's internal components (it's config, so it has special access).