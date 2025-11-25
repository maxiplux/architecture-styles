# Vertical Slice Architecture

## Overview

Vertical Slice Architecture organizes code by **feature** rather than by technical layer. Instead of having horizontal layers (controllers, services, repositories) that span all features, each feature is a self-contained "slice" that cuts vertically through all layers. This approach was popularized by Jimmy Bogard and aligns well with how teams actually work on features.

The key insight is that when you work on a feature, you typically touch code across all layers anyway. By organizing code around features, you minimize the number of files you need to navigate and reduce coupling between unrelated features.

---

## Core Principles

1. **Feature-First Organization**: Code is organized by what it does (features/use cases) rather than by technical concerns (layers).

2. **Minimal Cross-Feature Dependencies**: Each slice should be as independent as possible. Shared code is minimized and explicitly managed.

3. **Colocation**: All code related to a feature lives together in one place. Request, handler, response, validation—all in the same folder.

4. **Right-Sized Abstractions**: Each feature uses only the abstractions it needs. No forcing every feature through the same generic interfaces.

5. **REPR Pattern**: Request → Endpoint → Response. Each endpoint handles one specific operation.

---

## Comparison with Layered Architecture

**Layered Architecture** (horizontal slicing):
```
controllers/
├── CategoryController.java      ← All category endpoints
├── ProductController.java       ← All product endpoints  
└── OrderController.java         ← All order endpoints

services/
├── CategoryService.java
├── ProductService.java
└── OrderService.java

repositories/
├── CategoryRepository.java
├── ProductRepository.java
└── OrderRepository.java
```

**Vertical Slice Architecture** (vertical slicing):
```
features/
├── get_categories/              ← Everything for "Get Categories"
│   ├── GetCategoriesEndpoint.java
│   ├── GetCategoriesHandler.java
│   └── CategoryResponse.java
├── search_products/             ← Everything for "Search Products"
│   ├── SearchProductsEndpoint.java
│   ├── SearchProductsHandler.java
│   ├── ProductFilter.java
│   └── ProductResponse.java
└── create_order/                ← Everything for "Create Order"
    ├── CreateOrderEndpoint.java
    ├── CreateOrderHandler.java
    ├── CreateOrderRequest.java
    ├── CreateOrderValidator.java
    └── OrderResponse.java
```

---

## Target Package Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── features/                                 # FEATURE SLICES
│   ├── categories/
│   │   └── get_all/                         # Feature: Get All Categories
│   │       ├── GetAllCategoriesEndpoint.java
│   │       ├── GetAllCategoriesHandler.java
│   │       └── CategoryResponse.java
│   │
│   ├── products/
│   │   ├── search/                          # Feature: Search Products
│   │   │   ├── SearchProductsEndpoint.java
│   │   │   ├── SearchProductsHandler.java
│   │   │   ├── ProductFilter.java
│   │   │   ├── ProductResponse.java
│   │   │   └── ProductPageResponse.java
│   │   └── get_by_id/                       # Feature: Get Product by ID
│   │       ├── GetProductByIdEndpoint.java
│   │       ├── GetProductByIdHandler.java
│   │       └── ProductDetailResponse.java
│   │
│   └── orders/
│       ├── create/                          # Feature: Create Order
│       │   ├── CreateOrderEndpoint.java
│       │   ├── CreateOrderHandler.java
│       │   ├── CreateOrderRequest.java
│       │   ├── CreateOrderValidator.java
│       │   ├── OrderItemRequest.java
│       │   ├── ShippingAddressRequest.java
│       │   └── CreateOrderResponse.java
│       └── get_by_id/                       # Feature: Get Order by ID
│           ├── GetOrderByIdEndpoint.java
│           ├── GetOrderByIdHandler.java
│           └── OrderDetailResponse.java
│
├── shared/                                   # SHARED INFRASTRUCTURE
│   ├── persistence/
│   │   ├── entity/                          # JPA Entities (shared)
│   │   │   ├── CategoryEntity.java
│   │   │   ├── ProductEntity.java
│   │   │   ├── OrderEntity.java
│   │   │   └── OrderItemEntity.java
│   │   └── repository/                      # JPA Repositories (shared)
│   │       ├── CategoryRepository.java
│   │       ├── ProductRepository.java
│   │       └── OrderRepository.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── NotFoundException.java
│   │   ├── BadRequestException.java
│   │   └── ConflictException.java
│   ├── validation/
│   │   └── ValidationUtils.java
│   └── specification/
│       └── ProductSpecifications.java
│
└── config/                                   # APPLICATION CONFIGURATION
    ├── OpenApiConfig.java
    └── DataInitializer.java
```

---

## Feature Slice Implementation

Each feature slice contains everything needed to handle one specific use case. Let's look at each feature in detail.

### Feature: Get All Categories

This is a simple read operation that returns all active categories.

```java
// features/categories/get_all/CategoryResponse.java
// Response DTO specific to this feature
public record CategoryResponse(
    Long id,
    String name,
    String description,
    boolean active,
    OffsetDateTime createdAt
) {
    // Factory method to create from entity
    public static CategoryResponse fromEntity(CategoryEntity entity) {
        return new CategoryResponse(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }
}
```

```java
// features/categories/get_all/GetAllCategoriesHandler.java
// Handler contains the business logic for this specific feature
@Component
@RequiredArgsConstructor
public class GetAllCategoriesHandler {
    
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> handle() {
        return categoryRepository.findByActiveTrue().stream()
            .map(CategoryResponse::fromEntity)
            .toList();
    }
}
```

```java
// features/categories/get_all/GetAllCategoriesEndpoint.java
// Endpoint is the HTTP entry point - thin wrapper around handler
@Tag(name = "Categories", description = "Product category management endpoints")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class GetAllCategoriesEndpoint {
    
    private final GetAllCategoriesHandler handler;

    @Operation(
        summary = "Get all product categories",
        description = "Retrieves all active product categories"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> execute() {
        List<CategoryResponse> categories = handler.handle();
        return ResponseEntity.ok(categories);
    }
}
```

### Feature: Search Products

A more complex feature with filtering, pagination, and sorting.

```java
// features/products/search/ProductFilter.java
// Request parameters encapsulated in a filter object
public record ProductFilter(
    Long categoryId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean inStock,
    Boolean active
) {
    // Provide defaults for null values
    public ProductFilter {
        if (active == null) {
            active = true;  // Default to active products only
        }
    }
    
    // Factory for building from request parameters
    public static ProductFilter of(Long categoryId, String name, BigDecimal minPrice,
                                    BigDecimal maxPrice, Boolean inStock, Boolean active) {
        return new ProductFilter(categoryId, name, minPrice, maxPrice, inStock, active);
    }
}
```

```java
// features/products/search/ProductResponse.java
public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Long categoryId,
    String categoryName,
    Integer stock,
    String imageUrl,
    boolean active
) {
    public static ProductResponse fromEntity(ProductEntity entity) {
        return new ProductResponse(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getCategory().getId(),
            entity.getCategory().getName(),
            entity.getStock(),
            entity.getImageUrl(),
            entity.isActive()
        );
    }
}
```

```java
// features/products/search/ProductPageResponse.java
// Wrapper for paginated results
public record ProductPageResponse(
    List<ProductResponse> content,
    long totalElements,
    int totalPages,
    int number,
    int size,
    boolean first,
    boolean last
) {
    public static ProductPageResponse fromPage(Page<ProductEntity> page) {
        List<ProductResponse> content = page.getContent().stream()
            .map(ProductResponse::fromEntity)
            .toList();
        
        return new ProductPageResponse(
            content,
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
// features/products/search/SearchProductsHandler.java
@Component
@RequiredArgsConstructor
public class SearchProductsHandler {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductPageResponse handle(ProductFilter filter, Pageable pageable) {
        // Validate category exists if specified
        if (filter.categoryId() != null) {
            if (!categoryRepository.existsById(filter.categoryId())) {
                throw new NotFoundException("Category not found: " + filter.categoryId());
            }
        }

        // Build specification for dynamic query
        Specification<ProductEntity> spec = Specification.allOf(
            ProductSpecifications.hasCategory(filter.categoryId()),
            ProductSpecifications.nameLike(filter.name()),
            ProductSpecifications.priceBetween(filter.minPrice(), filter.maxPrice()),
            ProductSpecifications.inStock(filter.inStock()),
            ProductSpecifications.isActive(filter.active())
        );

        // Execute query with pagination
        Page<ProductEntity> page = productRepository.findAll(spec, pageable);
        
        return ProductPageResponse.fromPage(page);
    }
}
```

```java
// features/products/search/SearchProductsEndpoint.java
@Tag(name = "Products", description = "Product catalog management endpoints")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class SearchProductsEndpoint {
    
    private final SearchProductsHandler handler;

    @Operation(
        summary = "Search and filter products",
        description = "Search products with optional filters for category, name, price range, and stock availability"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ProductPageResponse> execute(
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            
            @Parameter(description = "Filter by product name (partial match)")
            @RequestParam(required = false) String name,
            
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) BigDecimal minPrice,
            
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) BigDecimal maxPrice,
            
            @Parameter(description = "Filter to show only products in stock")
            @RequestParam(required = false) Boolean inStock,
            
            @Parameter(description = "Filter by active status (default: true)")
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            
            @ParameterObject Pageable pageable
    ) {
        ProductFilter filter = ProductFilter.of(categoryId, name, minPrice, maxPrice, inStock, active);
        ProductPageResponse response = handler.handle(filter, pageable);
        return ResponseEntity.ok(response);
    }
}
```

### Feature: Create Order

The most complex feature with validation, business logic, and multiple entity interactions.

```java
// features/orders/create/OrderItemRequest.java
public record OrderItemRequest(
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 99, message = "Quantity cannot exceed 99")
    Integer quantity
) {}
```

```java
// features/orders/create/ShippingAddressRequest.java
public record ShippingAddressRequest(
    @NotBlank(message = "Street is required")
    @Size(max = 200, message = "Street cannot exceed 200 characters")
    String street,
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    String city,
    
    @Size(max = 100, message = "State cannot exceed 100 characters")
    String state,
    
    @NotBlank(message = "Zip code is required")
    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    String zipCode,
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    String country
) {}
```

```java
// features/orders/create/CreateOrderRequest.java
public record CreateOrderRequest(
    @NotNull(message = "Customer ID is required")
    Long customerId,
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    List<OrderItemRequest> items,
    
    @NotNull(message = "Shipping address is required")
    @Valid
    ShippingAddressRequest shippingAddress
) {}
```

```java
// features/orders/create/CreateOrderResponse.java
public record CreateOrderResponse(
    Long orderId,
    String status,
    List<OrderItemResponse> items,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total,
    OffsetDateTime createdAt
) {
    public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
    ) {
        public static OrderItemResponse fromEntity(OrderItemEntity entity) {
            return new OrderItemResponse(
                entity.getProduct().getId(),
                entity.getProduct().getName(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getSubtotal()
            );
        }
    }
    
    public static CreateOrderResponse fromEntity(OrderEntity entity) {
        List<OrderItemResponse> items = entity.getItems().stream()
            .map(OrderItemResponse::fromEntity)
            .toList();
        
        return new CreateOrderResponse(
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

```java
// features/orders/create/CreateOrderValidator.java
// Custom validator for complex business rules beyond annotation validation
@Component
@RequiredArgsConstructor
public class CreateOrderValidator {
    
    private final ProductRepository productRepository;

    public void validate(CreateOrderRequest request, List<ProductEntity> products) {
        List<String> errors = new ArrayList<>();
        
        // Check all products exist
        Set<Long> requestedIds = request.items().stream()
            .map(OrderItemRequest::productId)
            .collect(Collectors.toSet());
        Set<Long> foundIds = products.stream()
            .map(ProductEntity::getId)
            .collect(Collectors.toSet());
        
        Set<Long> missingIds = new HashSet<>(requestedIds);
        missingIds.removeAll(foundIds);
        
        if (!missingIds.isEmpty()) {
            errors.add("Products not found: " + missingIds);
        }
        
        // Check each product's availability
        for (OrderItemRequest item : request.items()) {
            ProductEntity product = products.stream()
                .filter(p -> p.getId().equals(item.productId()))
                .findFirst()
                .orElse(null);
            
            if (product != null) {
                if (!product.isActive()) {
                    errors.add("Product " + product.getId() + " (" + product.getName() + ") is not active");
                }
                if (product.getStock() < item.quantity()) {
                    errors.add("Insufficient stock for product " + product.getId() + 
                              " (" + product.getName() + "). Available: " + product.getStock() + 
                              ", Requested: " + item.quantity());
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("; ", errors));
        }
    }
}
```

```java
// features/orders/create/CreateOrderHandler.java
@Component
@RequiredArgsConstructor
public class CreateOrderHandler {
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CreateOrderValidator validator;

    @Transactional
    public CreateOrderResponse handle(CreateOrderRequest request) {
        // 1. Load all products
        List<Long> productIds = request.items().stream()
            .map(OrderItemRequest::productId)
            .toList();
        List<ProductEntity> products = productRepository.findAllById(productIds);
        
        // 2. Validate business rules
        validator.validate(request, products);
        
        // 3. Calculate totals and create order items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : request.items()) {
            ProductEntity product = products.stream()
                .filter(p -> p.getId().equals(itemRequest.productId()))
                .findFirst()
                .orElseThrow();
            
            BigDecimal lineSubtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(itemRequest.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
            
            subtotal = subtotal.add(lineSubtotal);
            
            OrderItemEntity orderItem = OrderItemEntity.builder()
                .product(product)
                .quantity(itemRequest.quantity())
                .unitPrice(product.getPrice())
                .subtotal(lineSubtotal)
                .build();
            orderItems.add(orderItem);
        }
        
        // 4. Calculate tax and total
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        
        // 5. Create order
        OrderEntity order = OrderEntity.builder()
            .customerId(request.customerId())
            .status(OrderStatus.PENDING)
            .subtotal(subtotal)
            .tax(tax)
            .total(total)
            .shippingStreet(request.shippingAddress().street())
            .shippingCity(request.shippingAddress().city())
            .shippingState(request.shippingAddress().state())
            .shippingZipCode(request.shippingAddress().zipCode())
            .shippingCountry(request.shippingAddress().country())
            .items(new ArrayList<>())
            .build();
        
        // 6. Link items to order
        for (OrderItemEntity item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }
        
        // 7. Save order
        OrderEntity savedOrder = orderRepository.save(order);
        
        // 8. Update product stock
        for (OrderItemEntity item : savedOrder.getItems()) {
            ProductEntity product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }
        
        // 9. Return response
        return CreateOrderResponse.fromEntity(savedOrder);
    }
}
```

```java
// features/orders/create/CreateOrderEndpoint.java
@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class CreateOrderEndpoint {
    
    private final CreateOrderHandler handler;

    @Operation(
        summary = "Create a new order",
        description = "Creates a new customer order with the specified items and shipping address. " +
                      "Validates product availability and stock levels before processing."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponse> execute(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = handler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

## Shared Infrastructure

Code that's genuinely shared across multiple features lives in the `shared/` directory.

### JPA Entities

```java
// shared/persistence/entity/CategoryEntity.java
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryEntity {
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
// shared/persistence/entity/ProductEntity.java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {
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
    private CategoryEntity category;
}
```

### Repositories

```java
// shared/persistence/repository/CategoryRepository.java
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByActiveTrue();
}
```

```java
// shared/persistence/repository/ProductRepository.java
public interface ProductRepository extends 
    JpaRepository<ProductEntity, Long>, 
    JpaSpecificationExecutor<ProductEntity> {
}
```

```java
// shared/persistence/repository/OrderRepository.java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
```

### Specifications

```java
// shared/specification/ProductSpecifications.java
public final class ProductSpecifications {
    
    private ProductSpecifications() {}

    public static Specification<ProductEntity> hasCategory(Long categoryId) {
        return (root, query, cb) ->
            categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<ProductEntity> nameLike(String name) {
        return (root, query, cb) ->
            (name == null || name.isBlank()) ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<ProductEntity> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    public static Specification<ProductEntity> inStock(Boolean inStock) {
        return (root, query, cb) ->
            (inStock == null || !inStock) ? null : cb.greaterThan(root.get("stock"), 0);
    }

    public static Specification<ProductEntity> isActive(Boolean active) {
        return (root, query, cb) ->
            active == null ? null : cb.equal(root.get("active"), active);
    }
}
```

### Exception Handling

```java
// shared/exception/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleConflict(ConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
```
---

## MapStruct Integration

In Vertical Slice Architecture, the primary goal is feature colocation. While simple mappings can be handled by static factory methods on DTOs, MapStruct becomes valuable when transformations within a slice become more complex. Instead of creating a shared `mapper` package, each feature slice that needs a mapper contains its own `*.mapper` package, reinforcing feature isolation.

### Adding MapStruct Dependency

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

### Feature-Specific Mappers

Mappers live inside the feature slice they serve. This prevents creating a shared abstraction that all features must conform to and keeps the logic colocated.

#### Example: Mapper for Create Order Feature

```java
// features/orders/create/mapper/CreateOrderMapper.java
@Mapper(componentModel = "spring")
public interface CreateOrderMapper {
    
    // Map from shared JPA entity to feature-specific response DTO
    @Mapping(source = "items", target = "items")
    CreateOrderResponse toResponse(OrderEntity entity);
    
    // Nested mapping for items
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    CreateOrderResponse.OrderItemResponse toItemResponse(OrderItemEntity entity);
}
```

### Using Mappers in Handlers

The feature handler uses the injected mapper, but the mapper itself is internal to the feature slice.

```java
// features/orders/create/CreateOrderHandler.java
@Component
@RequiredArgsConstructor
public class CreateOrderHandler {
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CreateOrderValidator validator;
    private final CreateOrderMapper mapper; // Injected mapper

    @Transactional
    public CreateOrderResponse handle(CreateOrderRequest request) {
        // ... (business logic for validation and order creation) ...
        
        OrderEntity savedOrder = orderRepository.save(order);
        
        // ... (logic for updating stock) ...
        
        // Use MapStruct for the final transformation
        return mapper.toResponse(savedOrder);
    }
}
```

The key benefit is that if the `CreateOrder` feature needs a different response shape than `GetOrderById`, each feature can have its own mapper and DTOs without affecting the other. This aligns perfectly with the goals of Vertical Slice Architecture.

---

## Testing Strategy

Vertical Slice Architecture encourages testing features as complete, isolated units. The most valuable tests are integration tests that exercise an entire slice from the endpoint down to the database. This approach verifies that all pieces of a feature work together correctly.

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

### Feature Slice Integration Tests

The primary testing strategy is to write integration tests for each feature slice. These tests use `SpringBootTest` to load the application context and Testcontainers to provide a real database.

```java
// features/orders/create/CreateOrderIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CreateOrderIntegrationTest {

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
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Clean up and set up test data before each test
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        
        CategoryEntity category = categoryRepository.save(
            CategoryEntity.builder().name("Electronics").active(true).build()
        );
        
        productRepository.save(
            ProductEntity.builder()
                .name("Gaming Laptop")
                .price(new BigDecimal("1299.99"))
                .stock(10)
                .active(true)
                .category(category)
                .build()
        );
    }

    @Test
    void createOrder_withValidData_returnsCreatedOrderAndUpdatesStock() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(new OrderItemRequest(1L, 2)),
            new ShippingAddressRequest("123 Main St", "City", "ST", "12345", "USA")
        );

        // When
        ResponseEntity<CreateOrderResponse> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            CreateOrderResponse.class
        );

        // Then - verify HTTP response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING", response.getBody().status());
        assertEquals(1, response.getBody().items().size());
        assertTrue(response.getBody().total().compareTo(BigDecimal.ZERO) > 0);

        // Verify side effect: stock was updated in the database
        ProductEntity product = productRepository.findById(1L).orElseThrow();
        assertEquals(8, product.getStock()); // Initial stock was 10, ordered 2
    }

    @Test
    void createOrder_withInsufficientStock_returnsBadRequest() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(new OrderItemRequest(1L, 20)), // Request 20, but only 10 in stock
            new ShippingAddressRequest("123 Main St", "City", "ST", "12345", "USA")
        );

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Insufficient stock"));
    }

    @Test
    void createOrder_withInactiveProduct_returnsBadRequest() {
        // Given - set product to inactive
        ProductEntity product = productRepository.findById(1L).orElseThrow();
        product.setActive(false);
        productRepository.save(product);

        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(new OrderItemRequest(1L, 1)),
            new ShippingAddressRequest("123 Main St", "City", "ST", "12345", "USA")
        );

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("is not active"));
    }
}
```

### Handler Unit Tests (Optional)

If a handler contains particularly complex logic that is difficult to test via an integration test, you can write a unit test for the handler by mocking its dependencies (e.g., repositories).

```java
// features/orders/create/CreateOrderHandlerTest.java
@ExtendWith(MockitoExtension.class)
class CreateOrderHandlerTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CreateOrderValidator validator;
    @Mock
    private CreateOrderMapper mapper;
    
    @InjectMocks
    private CreateOrderHandler handler;

    @Test
    void handle_whenValidatorPasses_savesOrderAndUpdatesStock() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(new OrderItemRequest(1L, 1)),
            new ShippingAddressRequest("123 Main St", "City", "ST", "12345", "USA")
        );
        ProductEntity product = ProductEntity.builder().id(1L).name("Laptop").price(BigDecimal.TEN).stock(5).active(true).build();
        
        when(productRepository.findAllById(anyList())).thenReturn(List.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        handler.handle(request);

        // Then
        verify(validator).validate(request, List.of(product));
        verify(orderRepository).save(any(OrderEntity.class));
        verify(productRepository).save(product); // Verify stock update was called
        assertEquals(4, product.getStock());
    }
}
```

### Architecture Tests

Use ArchUnit to enforce boundaries between feature slices.

```java
// architecture/VerticalSliceArchitectureTest.java
@AnalyzeClasses(packages = "app.quantun.architecture.features")
public class VerticalSliceArchitectureTest {

    @ArchTest
    public static final ArchRule slices_should_not_depend_on_each_other =
        slices()
            .matching("..features.(*)..")
            .should().beFreeOfCycles()
            .andShould().notDependOnEachOther();
}
```

---

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              FEATURE SLICES                                      │
│                                                                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                   │
│  │  get_categories │  │ search_products │  │  create_order   │                   │
│  │                 │  │                 │  │                 │                   │
│  │  Endpoint       │  │  Endpoint       │  │  Endpoint       │                   │
│  │  Handler        │  │  Handler        │  │  Handler        │                   │
│  │  Response       │  │  Filter         │  │  Request        │                   │
│  │                 │  │  Response       │  │  Validator      │                   │
│  │                 │  │  PageResponse   │  │  Response       │                   │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘                   │
│           │                    │                    │                            │
│           └────────────────────┼────────────────────┘                            │
│                                │                                                 │
│                                │ uses                                            │
│                                ▼                                                 │
└────────────────────────────────┬────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           SHARED INFRASTRUCTURE                                  │
│                                                                                  │
│   persistence/entity/              persistence/repository/                       │
│   - CategoryEntity                 - CategoryRepository                          │
│   - ProductEntity                  - ProductRepository                           │
│   - OrderEntity                    - OrderRepository                             │
│   - OrderItemEntity                                                              │
│                                                                                  │
│   specification/                   exception/                                    │
│   - ProductSpecifications          - GlobalExceptionHandler                      │
│                                    - NotFoundException                           │
│                                    - BadRequestException                         │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
                    ┌───────────────────────┐
                    │      Database         │
                    │     (PostgreSQL)      │
                    └───────────────────────┘
```

---

## Migration Steps

### Step 1: Create Feature Directory Structure

Create the `features/` directory with subdirectories for each feature:
```
features/
├── categories/get_all/
├── products/search/
└── orders/create/
```

### Step 2: Extract Get Categories Feature

1. Create `features/categories/get_all/CategoryResponse.java`
2. Create `features/categories/get_all/GetAllCategoriesHandler.java`
3. Create `features/categories/get_all/GetAllCategoriesEndpoint.java`
4. Move mapping and business logic from old service/controller

### Step 3: Extract Search Products Feature

1. Create `features/products/search/ProductFilter.java`
2. Create `features/products/search/ProductResponse.java`
3. Create `features/products/search/ProductPageResponse.java`
4. Create `features/products/search/SearchProductsHandler.java`
5. Create `features/products/search/SearchProductsEndpoint.java`

### Step 4: Extract Create Order Feature

1. Create request DTOs: `CreateOrderRequest.java`, `OrderItemRequest.java`, `ShippingAddressRequest.java`
2. Create `features/orders/create/CreateOrderResponse.java`
3. Create `features/orders/create/CreateOrderValidator.java`
4. Create `features/orders/create/CreateOrderHandler.java`
5. Create `features/orders/create/CreateOrderEndpoint.java`

### Step 5: Create Shared Infrastructure

1. Create `shared/persistence/entity/` with JPA entities
2. Create `shared/persistence/repository/` with Spring Data repositories
3. Create `shared/specification/ProductSpecifications.java`
4. Create `shared/exception/` with exception classes and handler

### Step 6: Cleanup and Configuration

1. Delete old controller, service, dto, repository packages
2. Update `DataInitializer` to use repositories directly
3. Verify all OpenAPI annotations are preserved
4. Test all endpoints

---

## When to Share Code

The key decision in Vertical Slice Architecture is deciding what belongs in a feature slice versus what should be shared.

**Keep in Feature Slice:**
- Request/Response DTOs specific to that feature
- Validation logic specific to that feature
- Handler business logic
- Mapping logic from entities to responses

**Put in Shared:**
- JPA Entities (they represent the database schema)
- Repositories (they're the data access abstraction)
- Specifications (if used by multiple features)
- Exception classes and handlers
- Cross-cutting concerns (logging, security, etc.)

**Rule of Thumb:** If you're tempted to share a response DTO between features, don't. Let each feature define exactly what it needs. Duplication in DTOs is okay—they evolve independently with their features.

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

When refactoring to Vertical Slice Architecture:

1. **Preserve All API Contracts**: Request/response formats must remain identical.
2. **One Endpoint Per Class**: Each endpoint class handles one HTTP operation.
3. **Handler Contains Logic**: Business logic lives in handlers, not endpoints.
4. **DTOs Are Feature-Specific**: Each feature has its own request/response types.
5. **Entities Are Shared**: JPA entities live in `shared/persistence/entity/`.
6. **Repositories Are Shared**: Spring Data repos live in `shared/persistence/repository/`.
7. **Validation Is Colocated**: Put validators in the same package as the feature.
8. **Mapping Is Local**: Use static factory methods on DTOs for entity→DTO mapping.
9. **OpenAPI Annotations**: Keep them on endpoint classes.
10. **DataInitializer**: Update to use shared repositories.