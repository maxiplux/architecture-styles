# Event-Driven Architecture (Spring Application Events)

## Overview

Event-Driven Architecture (EDA) is a design pattern where the flow of the application is determined by events—significant changes in state that other parts of the system might be interested in. Instead of components calling each other directly, they communicate by publishing and subscribing to events. This creates loose coupling and enables flexible, extensible systems.

This document focuses on implementing EDA using **Spring's ApplicationEventPublisher**, which provides an in-process, synchronous (or asynchronous with configuration) event mechanism. This is distinct from message brokers like Kafka or RabbitMQ—we're staying within a single JVM for simplicity while still gaining the architectural benefits of event-driven design.

---

## Core Principles

1. **Events Represent Facts**: Events describe something that has happened (past tense). They are immutable records of state changes. Examples: `OrderCreated`, `ProductStockDepleted`, `PaymentReceived`.

2. **Publishers Don't Know Subscribers**: The component publishing an event doesn't know (or care) who's listening. This is the key to loose coupling.

3. **Multiple Subscribers**: A single event can trigger multiple independent reactions. Adding new behavior often means just adding a new listener.

4. **Temporal Decoupling**: Publishers and subscribers don't need to be available at the same time (though with Spring events in a single JVM, they typically are).

5. **Single Responsibility**: Each listener handles one specific concern. This keeps code focused and testable.

---

## Spring Event Mechanism

### How Spring Events Work

```
┌─────────────────┐     publish()      ┌──────────────────────┐
│    Publisher    │ ─────────────────► │  ApplicationEvent    │
│   (Service)     │                    │   Publisher          │
└─────────────────┘                    └──────────┬───────────┘
                                                  │
                                                  │ dispatches to
                                                  ▼
                    ┌─────────────────────────────────────────────────┐
                    │                                                 │
           ┌────────┴────────┐  ┌────────────────┐  ┌────────────────┴────────┐
           │   Listener A    │  │   Listener B   │  │     Listener C          │
           │  (Inventory)    │  │ (Notification) │  │    (Analytics)          │
           └─────────────────┘  └────────────────┘  └─────────────────────────┘
```

### Key Annotations

- `@EventListener`: Marks a method as an event listener (synchronous by default)
- `@TransactionalEventListener`: Listener that respects transaction boundaries
- `@Async`: Makes the listener execute asynchronously (requires `@EnableAsync`)

### Transaction Phases

`@TransactionalEventListener` supports different phases:

- `AFTER_COMMIT` (default): Executes after the transaction commits successfully
- `AFTER_ROLLBACK`: Executes if the transaction rolls back
- `AFTER_COMPLETION`: Executes after commit or rollback
- `BEFORE_COMMIT`: Executes before the transaction commits

---

## Target Package Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── domain/                                   # DOMAIN MODEL
│   ├── entity/
│   │   ├── Category.java
│   │   ├── Product.java
│   │   ├── CustomerOrder.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── repository/
│   │   ├── CategoryRepository.java
│   │   ├── ProductRepository.java
│   │   └── OrderRepository.java
│   └── specification/
│       └── ProductSpecifications.java
│
├── event/                                    # EVENT INFRASTRUCTURE
│   ├── DomainEvent.java                     # Base event interface
│   ├── EventPublisher.java                  # Publishing abstraction
│   ├── SpringEventPublisher.java            # Spring implementation
│   │
│   ├── order/                               # Order-related events
│   │   ├── OrderCreatedEvent.java
│   │   ├── OrderConfirmedEvent.java
│   │   ├── OrderShippedEvent.java
│   │   ├── OrderDeliveredEvent.java
│   │   └── OrderCancelledEvent.java
│   │
│   └── catalog/                             # Catalog-related events
│       ├── ProductCreatedEvent.java
│       ├── ProductStockDepletedEvent.java
│       └── ProductPriceChangedEvent.java
│
├── service/                                  # BUSINESS LOGIC (Publishers)
│   ├── CategoryService.java
│   ├── ProductService.java
│   └── OrderService.java
│
├── listener/                                 # EVENT LISTENERS (Subscribers)
│   ├── inventory/
│   │   └── InventoryEventListener.java      # Stock management reactions
│   ├── notification/
│   │   └── NotificationEventListener.java   # Email/SMS notifications
│   ├── analytics/
│   │   └── AnalyticsEventListener.java      # Tracking and metrics
│   └── audit/
│       └── AuditEventListener.java          # Audit logging
│
├── web/                                      # REST API
│   ├── controller/
│   │   ├── CategoryController.java
│   │   ├── ProductController.java
│   │   └── OrderController.java
│   ├── dto/
│   │   ├── CategoryDTO.java
│   │   ├── ProductDTO.java
│   │   ├── ProductFilter.java
│   │   └── order/
│   │       ├── OrderCreateRequest.java
│   │       ├── OrderItemRequest.java
│   │       ├── ShippingAddressDTO.java
│   │       └── OrderResponse.java
│   └── exception/
│       └── GlobalExceptionHandler.java
│
└── config/                                   # CONFIGURATION
    ├── AsyncConfig.java                     # Enable async event processing
    ├── OpenApiConfig.java
    └── DataInitializer.java
```

---

## Event Definitions

### Base Event Interface

```java
// event/DomainEvent.java
// All domain events implement this interface
public interface DomainEvent {
    
    /**
     * Unique identifier for this event instance
     */
    UUID getEventId();
    
    /**
     * When this event occurred
     */
    OffsetDateTime getOccurredOn();
    
    /**
     * Human-readable event type name
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}
```

### Order Events

```java
// event/order/OrderCreatedEvent.java
// Published when a new order is created
public record OrderCreatedEvent(
    UUID eventId,
    Long orderId,
    Long customerId,
    List<OrderItemInfo> items,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total,
    ShippingAddressInfo shippingAddress,
    OffsetDateTime occurredOn
) implements DomainEvent {
    
    // Nested record for item information
    public record OrderItemInfo(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
    ) {}
    
    // Nested record for shipping address
    public record ShippingAddressInfo(
        String street,
        String city,
        String state,
        String zipCode,
        String country
    ) {}
    
    // Factory method for creating the event
    public static OrderCreatedEvent from(CustomerOrder order) {
        List<OrderItemInfo> items = order.getItems().stream()
            .map(item -> new OrderItemInfo(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
            ))
            .toList();
        
        ShippingAddressInfo address = new ShippingAddressInfo(
            order.getShippingStreet(),
            order.getShippingCity(),
            order.getShippingState(),
            order.getShippingZipCode(),
            order.getShippingCountry()
        );
        
        return new OrderCreatedEvent(
            UUID.randomUUID(),
            order.getId(),
            order.getCustomerId(),
            items,
            order.getSubtotal(),
            order.getTax(),
            order.getTotal(),
            address,
            OffsetDateTime.now()
        );
    }
    
    @Override
    public UUID getEventId() {
        return eventId;
    }
    
    @Override
    public OffsetDateTime getOccurredOn() {
        return occurredOn;
    }
}
```

```java
// event/order/OrderConfirmedEvent.java
public record OrderConfirmedEvent(
    UUID eventId,
    Long orderId,
    Long customerId,
    OffsetDateTime occurredOn
) implements DomainEvent {
    
    public static OrderConfirmedEvent from(CustomerOrder order) {
        return new OrderConfirmedEvent(
            UUID.randomUUID(),
            order.getId(),
            order.getCustomerId(),
            OffsetDateTime.now()
        );
    }
    
    @Override
    public UUID getEventId() { return eventId; }
    
    @Override
    public OffsetDateTime getOccurredOn() { return occurredOn; }
}
```

```java
// event/order/OrderCancelledEvent.java
public record OrderCancelledEvent(
    UUID eventId,
    Long orderId,
    Long customerId,
    List<CancelledItemInfo> items,
    String cancellationReason,
    OffsetDateTime occurredOn
) implements DomainEvent {
    
    public record CancelledItemInfo(
        Long productId,
        Integer quantity
    ) {}
    
    public static OrderCancelledEvent from(CustomerOrder order, String reason) {
        List<CancelledItemInfo> items = order.getItems().stream()
            .map(item -> new CancelledItemInfo(
                item.getProduct().getId(),
                item.getQuantity()
            ))
            .toList();
        
        return new OrderCancelledEvent(
            UUID.randomUUID(),
            order.getId(),
            order.getCustomerId(),
            items,
            reason,
            OffsetDateTime.now()
        );
    }
    
    @Override
    public UUID getEventId() { return eventId; }
    
    @Override
    public OffsetDateTime getOccurredOn() { return occurredOn; }
}
```

### Catalog Events

```java
// event/catalog/ProductStockDepletedEvent.java
// Published when a product's stock falls to zero or below a threshold
public record ProductStockDepletedEvent(
    UUID eventId,
    Long productId,
    String productName,
    Integer currentStock,
    Integer threshold,
    OffsetDateTime occurredOn
) implements DomainEvent {
    
    private static final int DEFAULT_THRESHOLD = 5;
    
    public static ProductStockDepletedEvent from(Product product) {
        return new ProductStockDepletedEvent(
            UUID.randomUUID(),
            product.getId(),
            product.getName(),
            product.getStock(),
            DEFAULT_THRESHOLD,
            OffsetDateTime.now()
        );
    }
    
    public boolean isOutOfStock() {
        return currentStock <= 0;
    }
    
    public boolean isBelowThreshold() {
        return currentStock <= threshold;
    }
    
    @Override
    public UUID getEventId() { return eventId; }
    
    @Override
    public OffsetDateTime getOccurredOn() { return occurredOn; }
}
```

```java
// event/catalog/ProductPriceChangedEvent.java
public record ProductPriceChangedEvent(
    UUID eventId,
    Long productId,
    String productName,
    BigDecimal previousPrice,
    BigDecimal newPrice,
    BigDecimal changePercentage,
    OffsetDateTime occurredOn
) implements DomainEvent {
    
    public static ProductPriceChangedEvent from(Product product, BigDecimal previousPrice) {
        BigDecimal change = product.getPrice().subtract(previousPrice);
        BigDecimal changePercentage = previousPrice.compareTo(BigDecimal.ZERO) != 0
            ? change.divide(previousPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        return new ProductPriceChangedEvent(
            UUID.randomUUID(),
            product.getId(),
            product.getName(),
            previousPrice,
            product.getPrice(),
            changePercentage,
            OffsetDateTime.now()
        );
    }
    
    public boolean isPriceIncrease() {
        return newPrice.compareTo(previousPrice) > 0;
    }
    
    public boolean isPriceDecrease() {
        return newPrice.compareTo(previousPrice) < 0;
    }
    
    @Override
    public UUID getEventId() { return eventId; }
    
    @Override
    public OffsetDateTime getOccurredOn() { return occurredOn; }
}
```

---

## Event Publisher

### Publisher Abstraction

```java
// event/EventPublisher.java
// Abstraction over Spring's ApplicationEventPublisher
// Allows for easier testing and potential future changes to event infrastructure
public interface EventPublisher {
    
    /**
     * Publishes an event to all registered listeners.
     * 
     * @param event the domain event to publish
     */
    void publish(DomainEvent event);
    
    /**
     * Publishes multiple events in order.
     * 
     * @param events the events to publish
     */
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
```

### Spring Implementation

```java
// event/SpringEventPublisher.java
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {} with ID: {}", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
        log.info("Published event: {} [id={}]", event.getEventType(), event.getEventId());
    }
    
    @Override
    public void publishAll(List<DomainEvent> events) {
        log.debug("Publishing {} events", events.size());
        events.forEach(this::publish);
    }
}
```

---

## Services (Publishers)

Services contain business logic and publish events when significant state changes occur.

```java
// service/OrderService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("Creating order for customer: {}", request.customerId());
        
        // 1. Load products
        List<Long> productIds = request.items().stream()
            .map(OrderItemRequest::productId)
            .toList();
        List<Product> products = productRepository.findAllById(productIds);
        
        // 2. Validate all products exist
        if (products.size() != productIds.size()) {
            throw new BadRequestException("One or more products not found");
        }
        
        // 3. Validate availability and build order
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        List<DomainEvent> eventsToPublish = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(itemRequest.productId()))
                .findFirst()
                .orElseThrow();
            
            // Validate product is active
            if (!product.isActive()) {
                throw new ConflictException("Product is inactive: " + product.getId());
            }
            
            // Validate stock
            if (product.getStock() < itemRequest.quantity()) {
                throw new ConflictException(
                    "Insufficient stock for product " + product.getId() +
                    ". Available: " + product.getStock() + ", Requested: " + itemRequest.quantity()
                );
            }
            
            // Calculate line item
            BigDecimal lineSubtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(itemRequest.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
            
            subtotal = subtotal.add(lineSubtotal);
            
            OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(itemRequest.quantity())
                .unitPrice(product.getPrice())
                .subtotal(lineSubtotal)
                .build();
            orderItems.add(orderItem);
        }
        
        // 4. Calculate totals
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        
        // 5. Create order entity
        CustomerOrder order = CustomerOrder.builder()
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
        
        // Link items to order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }
        
        // 6. Save order
        CustomerOrder savedOrder = orderRepository.save(order);
        log.info("Order saved with ID: {}", savedOrder.getId());
        
        // 7. Update product stock and collect stock events
        for (OrderItem item : savedOrder.getItems()) {
            Product product = item.getProduct();
            int previousStock = product.getStock();
            product.setStock(previousStock - item.getQuantity());
            productRepository.save(product);
            
            // Check if stock is depleted
            if (product.getStock() <= 5) { // Threshold
                eventsToPublish.add(ProductStockDepletedEvent.from(product));
            }
        }
        
        // 8. Publish OrderCreatedEvent (this is the main event)
        eventPublisher.publish(OrderCreatedEvent.from(savedOrder));
        
        // 9. Publish any stock depletion events
        eventsToPublish.forEach(eventPublisher::publish);
        
        // 10. Return response
        return OrderResponse.from(savedOrder);
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only pending orders can be confirmed");
        }
        
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        
        // Publish event
        eventPublisher.publish(OrderConfirmedEvent.from(order));
        log.info("Order {} confirmed", orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        CustomerOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        
        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED) {
            throw new ConflictException("Cannot cancel shipped or delivered orders");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        // Publish cancellation event - listeners will handle stock restoration
        eventPublisher.publish(OrderCancelledEvent.from(order, reason));
        log.info("Order {} cancelled. Reason: {}", orderId, reason);
    }
}
```

---

## Event Listeners (Subscribers)

Listeners react to events. Each listener focuses on a single concern.

### Inventory Listener

```java
// listener/inventory/InventoryEventListener.java
// Handles inventory-related reactions to events
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {
    
    private final ProductRepository productRepository;

    /**
     * Restores product stock when an order is cancelled.
     * Runs AFTER the transaction commits to ensure the order is actually cancelled.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Handling OrderCancelledEvent for order: {}", event.orderId());
        
        for (OrderCancelledEvent.CancelledItemInfo item : event.items()) {
            productRepository.findById(item.productId()).ifPresent(product -> {
                int previousStock = product.getStock();
                product.setStock(previousStock + item.quantity());
                productRepository.save(product);
                
                log.info("Restored {} units of product {} (stock: {} -> {})", 
                    item.quantity(), 
                    item.productId(), 
                    previousStock, 
                    product.getStock()
                );
            });
        }
    }

    /**
     * Logs when stock is depleted.
     * In a real system, this might trigger a reorder or alert.
     */
    @EventListener
    public void onStockDepleted(ProductStockDepletedEvent event) {
        if (event.isOutOfStock()) {
            log.warn("STOCK OUT: Product {} ({}) is out of stock!", 
                event.productId(), event.productName());
        } else if (event.isBelowThreshold()) {
            log.warn("LOW STOCK: Product {} ({}) has only {} units remaining", 
                event.productId(), event.productName(), event.currentStock());
        }
        
        // In a real application, you might:
        // - Send an alert to the purchasing department
        // - Automatically create a purchase order
        // - Update a dashboard
        // - Notify warehouse staff
    }
}
```

### Notification Listener

```java
// listener/notification/NotificationEventListener.java
// Handles notification-related reactions (email, SMS, push notifications)
@Component
@Slf4j
public class NotificationEventListener {

    /**
     * Sends order confirmation notification to customer.
     * Runs asynchronously to avoid blocking the main transaction.
     * Uses AFTER_COMMIT to ensure we only notify for successfully created orders.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Sending order confirmation for order: {} to customer: {}", 
            event.orderId(), event.customerId());
        
        // Simulate sending notification
        // In a real application:
        // - Look up customer email/phone
        // - Send email via SendGrid, SES, etc.
        // - Send SMS via Twilio, etc.
        // - Send push notification
        
        try {
            // Simulate async work
            Thread.sleep(100);
            log.info("Order confirmation sent for order: {}", event.orderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending notification for order: {}", event.orderId());
        }
    }

    /**
     * Sends shipping notification when order is shipped.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderShipped(OrderShippedEvent event) {
        log.info("Sending shipping notification for order: {}", event.orderId());
        // Send "Your order has shipped!" notification with tracking info
    }

    /**
     * Notifies customer when order is cancelled.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Sending cancellation notification for order: {} to customer: {}", 
            event.orderId(), event.customerId());
        // Send cancellation confirmation email
    }

    /**
     * Alerts purchasing team about low stock.
     */
    @Async
    @EventListener
    public void onStockDepleted(ProductStockDepletedEvent event) {
        if (event.isOutOfStock()) {
            log.info("Alerting team: Product {} is out of stock", event.productName());
            // Send urgent alert to purchasing team
        }
    }
}
```

### Analytics Listener

```java
// listener/analytics/AnalyticsEventListener.java
// Tracks metrics and analytics data
@Component
@Slf4j
public class AnalyticsEventListener {

    /**
     * Tracks order creation for analytics.
     * Runs asynchronously to avoid impacting order creation performance.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackOrderCreated(OrderCreatedEvent event) {
        log.debug("Tracking order created: {}", event.orderId());
        
        // In a real application:
        // - Send to analytics service (Segment, Mixpanel, etc.)
        // - Update real-time dashboards
        // - Feed into recommendation engine
        
        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("event", "order_created");
        analyticsData.put("order_id", event.orderId());
        analyticsData.put("customer_id", event.customerId());
        analyticsData.put("total", event.total());
        analyticsData.put("item_count", event.items().size());
        analyticsData.put("timestamp", event.occurredOn());
        
        log.info("Analytics tracked: {}", analyticsData);
    }

    /**
     * Tracks order cancellations for churn analysis.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackOrderCancelled(OrderCancelledEvent event) {
        log.debug("Tracking order cancellation: {}", event.orderId());
        
        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("event", "order_cancelled");
        analyticsData.put("order_id", event.orderId());
        analyticsData.put("customer_id", event.customerId());
        analyticsData.put("reason", event.cancellationReason());
        analyticsData.put("timestamp", event.occurredOn());
        
        log.info("Analytics tracked: {}", analyticsData);
    }

    /**
     * Tracks price changes for pricing analytics.
     */
    @Async
    @EventListener
    public void trackPriceChange(ProductPriceChangedEvent event) {
        log.debug("Tracking price change for product: {}", event.productId());
        
        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("event", "price_changed");
        analyticsData.put("product_id", event.productId());
        analyticsData.put("product_name", event.productName());
        analyticsData.put("previous_price", event.previousPrice());
        analyticsData.put("new_price", event.newPrice());
        analyticsData.put("change_percentage", event.changePercentage());
        analyticsData.put("direction", event.isPriceIncrease() ? "increase" : "decrease");
        
        log.info("Analytics tracked: {}", analyticsData);
    }
}
```

### Audit Listener

```java
// listener/audit/AuditEventListener.java
// Creates audit trail for compliance and debugging
@Component
@Slf4j
public class AuditEventListener {

    /**
     * Records all domain events in an audit log.
     * This listener catches ALL domain events for audit purposes.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void auditDomainEvent(DomainEvent event) {
        log.info("AUDIT: Event={}, EventId={}, OccurredOn={}", 
            event.getEventType(),
            event.getEventId(),
            event.getOccurredOn()
        );
        
        // In a real application:
        // - Persist to audit table
        // - Send to audit logging service (Splunk, ELK, etc.)
        // - Store in compliance system
        
        // Example of what you might persist:
        // AuditLog auditLog = AuditLog.builder()
        //     .eventId(event.getEventId())
        //     .eventType(event.getEventType())
        //     .eventData(toJson(event))
        //     .occurredOn(event.getOccurredOn())
        //     .build();
        // auditLogRepository.save(auditLog);
    }
}
```

---

## Configuration

### Async Configuration

```java
// config/AsyncConfig.java
// Enables asynchronous event processing
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-event-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        
        log.info("Async executor configured with core={}, max={}, queue={}", 
            executor.getCorePoolSize(), 
            executor.getMaxPoolSize(), 
            executor.getQueueCapacity()
        );
        
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async exception in method: {} with params: {}", 
                method.getName(), 
                Arrays.toString(params), 
                throwable
            );
        };
    }
}
```

---

## Web Layer

The web layer remains largely unchanged—it calls services which publish events.

```java
// web/controller/OrderController.java
@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    @Operation(summary = "Create a new order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or product not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock or product inactive")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Confirm an order")
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancel an order")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "Customer requested cancellation") String reason
    ) {
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok().build();
    }
}
```

---
## MapStruct Integration

In an Event-Driven Architecture, the events themselves become data contracts. It's crucial that these events are stable and don't expose internal domain details. MapStruct is excellent for transforming your rich internal domain entities into immutable, data-focused event records, ensuring your event contracts are clean and decoupled from your domain model.

### Adding MapStruct Dependency

```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

### Event Mappers

Instead of using static factory methods on the event records, you can create dedicated mappers. This is especially useful for complex events and keeps the mapping logic separate from the data structure.

```java
// event/order/mapper/OrderEventMapper.java
@Mapper(componentModel = "spring")
public interface OrderEventMapper {
    
    @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "occurredOn", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.customerId", target = "customerId")
    @Mapping(source = "order.items", target = "items")
    @Mapping(source = "order.subtotal", target = "subtotal")
    @Mapping(source = "order.tax", target = "tax")
    @Mapping(source = "order.total", target = "total")
    @Mapping(source = "order", target = "shippingAddress")
    OrderCreatedEvent toOrderCreatedEvent(CustomerOrder order);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderCreatedEvent.OrderItemInfo toOrderItemInfo(OrderItem item);
    
    default OrderCreatedEvent.ShippingAddressInfo toShippingAddressInfo(CustomerOrder order) {
        return new OrderCreatedEvent.ShippingAddressInfo(
            order.getShippingStreet(),
            order.getShippingCity(),
            order.getShippingState(),
            order.getShippingZipCode(),
            order.getShippingCountry()
        );
    }

    @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "occurredOn", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.customerId", target = "customerId")
    OrderConfirmedEvent toOrderConfirmedEvent(CustomerOrder order);
}
```

### Using Mappers in Services

The service can then use the injected mapper to create and publish events.

```java
// service/OrderService.java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final OrderEventMapper eventMapper; // Injected mapper

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // ... (business logic to create and save order) ...
        
        CustomerOrder savedOrder = orderRepository.save(order);
        
        // Use mapper to create the event
        OrderCreatedEvent event = eventMapper.toOrderCreatedEvent(savedOrder);
        
        // Publish event
        eventPublisher.publish(event);
        
        return OrderResponse.from(savedOrder);
    }
}
```

---

## Testing Strategy

Testing an event-driven system requires focusing on three key areas:
1.  **Publishers**: Do services publish the correct events when state changes?
2.  **Listeners**: Does a listener perform the correct action when it receives an event?
3.  **Integration**: Does the whole flow work, from initial action to all resulting side effects?

### Test Dependencies

```gradle
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'
    testImplementation 'org.awaitility:awaitility:4.2.0' // For testing async listeners
}
```

### Testing the Publisher (Unit Test)

Verify that a service publishes the correct event by mocking the `EventPublisher`.

```java
// service/OrderServiceTest.java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private OrderService orderService;
    
    @Captor
    private ArgumentCaptor<DomainEvent> eventCaptor;

    @Test
    void createOrder_publishesOrderCreatedEvent() {
        // Given
        CreateOrderRequest request = // ... create request
        Product product = // ... create product
        
        when(productRepository.findAllById(anyList())).thenReturn(List.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            CustomerOrder order = inv.getArgument(0);
            order.setId(1L);
            return order;
        });

        // When
        orderService.createOrder(request);

        // Then
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        DomainEvent publishedEvent = eventCaptor.getValue();
        
        assertInstanceOf(OrderCreatedEvent.class, publishedEvent);
        OrderCreatedEvent orderEvent = (OrderCreatedEvent) publishedEvent;
        assertEquals(1L, orderEvent.orderId());
    }
}
```

### Testing a Listener (Integration Test)

Test a listener's logic in isolation by directly invoking it with a test event.

```java
// listener/inventory/InventoryEventListenerTest.java
@SpringBootTest
@Testcontainers
class InventoryEventListenerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) { /* ... */ }
    
    @Autowired
    private InventoryEventListener listener;
    
    @Autowired
    private ProductRepository productRepository;

    @Test
    void onOrderCancelled_restoresProductStock() {
        // Given - a product with 5 items in stock
        Product product = productRepository.save(
            Product.builder().name("Test").price(BigDecimal.TEN).stock(5).active(true).build()
        );
        
        // An event indicating an order for 3 items was cancelled
        OrderCancelledEvent event = new OrderCancelledEvent(
            UUID.randomUUID(), 1L, 1L,
            List.of(new OrderCancelledEvent.CancelledItemInfo(product.getId(), 3)),
            "Test reason", OffsetDateTime.now()
        );

        // When
        listener.onOrderCancelled(event);

        // Then - stock should be restored from 5 to 8
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(8, updatedProduct.getStock());
    }
}
```

### Testing the Full Flow (End-to-End Integration Test)

Verify the entire chain from service call to listener execution.

```java
// service/OrderEventFlowIntegrationTest.java
@SpringBootTest
@Testcontainers
class OrderEventFlowIntegrationTest {

    // ... Testcontainers setup ...
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @SpyBean
    private NotificationEventListener notificationListener;
    
    @SpyBean
    private InventoryEventListener inventoryListener;

    @Test
    void createOrder_triggersNotificationAndAuditListeners() {
        // Given - set up a product
        Product product = productRepository.save(/* ... */);
        CreateOrderRequest request = // ... create valid request
        
        // When
        orderService.createOrder(request);

        // Then - use Awaitility for async listeners
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationListener).onOrderCreated(any(OrderCreatedEvent.class));
        });
    }

    @Test
    void cancelOrder_triggersInventoryListenerToRestoreStock() {
        // Given - create an order first
        CreateOrderRequest createRequest = // ...
        OrderResponse createdOrder = orderService.createOrder(createRequest);
        
        // When
        orderService.cancelOrder(createdOrder.orderId(), "Reason");

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(inventoryListener).onOrderCancelled(any(OrderCancelledEvent.class));
        });
        
        // And verify the final state
        Product product = productRepository.findById(1L).orElseThrow();
        assertEquals(10, product.getStock()); // Verify stock was restored
    }
}
```

---

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                WEB LAYER                                         │
│                                                                                  │
│   web/controller/                                                                │
│   - CategoryController                                                           │
│   - ProductController                                                            │
│   - OrderController                                                              │
│                                                                                  │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ calls
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SERVICE LAYER                                       │
│                           (Event Publishers)                                     │
│                                                                                  │
│   service/                                                                       │
│   - OrderService ──────────────► publishes ──────────────────┐                   │
│   - ProductService ────────────► publishes ──────────────────┤                   │
│   - CategoryService                                          │                   │
│                                                              │                   │
└──────────────────────────────────────────────────────────────┼──────────────────┘
                                                               │
                                         ┌─────────────────────┴─────────────────────┐
                                         │                                           │
                                         ▼                                           │
┌────────────────────────────────────────────────────────────────────────────────────┤
│                            EVENT INFRASTRUCTURE                                    │
│                                                                                    │
│   event/                                                                           │
│   - EventPublisher (interface)                                                     │
│   - SpringEventPublisher (implementation)                                          │
│                                                                                    │
│   event/order/                          event/catalog/                             │
│   - OrderCreatedEvent                   - ProductStockDepletedEvent                │
│   - OrderConfirmedEvent                 - ProductPriceChangedEvent                 │
│   - OrderCancelledEvent                                                            │
│                                                                                    │
└────────────────────────────────────────┬───────────────────────────────────────────┘
                                         │ dispatches to
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            EVENT LISTENERS                                       │
│                           (Event Subscribers)                                    │
│                                                                                  │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────────┐   │
│  │     Inventory       │  │    Notification     │  │      Analytics          │   │
│  │     Listener        │  │      Listener       │  │       Listener          │   │
│  │                     │  │                     │  │                         │   │
│  │ - Restore stock on  │  │ - Send order        │  │ - Track order metrics   │   │
│  │   cancellation      │  │   confirmations     │  │ - Monitor cancellations │   │
│  │ - Handle low stock  │  │ - Alert on low      │  │ - Price analytics       │   │
│  │   alerts            │  │   stock             │  │                         │   │
│  └─────────────────────┘  └─────────────────────┘  └─────────────────────────┘   │
│                                                                                  │
│  ┌─────────────────────┐                                                         │
│  │       Audit         │                                                         │
│  │      Listener       │                                                         │
│  │                     │                                                         │
│  │ - Log all events    │                                                         │
│  │ - Compliance trail  │                                                         │
│  └─────────────────────┘                                                         │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Event Flow Example: Creating an Order

```
1. Client sends POST /api/v1/orders
                │
                ▼
2. OrderController.create() calls OrderService.createOrder()
                │
                ▼
3. OrderService:
   a. Validates products and stock
   b. Creates and saves order
   c. Updates product stock
   d. Publishes OrderCreatedEvent
   e. Publishes ProductStockDepletedEvent (if applicable)
   f. Returns OrderResponse
                │
                ▼
4. Spring dispatches OrderCreatedEvent to listeners:
   
   InventoryEventListener     - (no action for creation)
   NotificationEventListener  - Sends confirmation email (async, after commit)
   AnalyticsEventListener     - Tracks order metrics (async, after commit)
   AuditEventListener         - Logs the event (after commit)
                │
                ▼
5. If stock was depleted, ProductStockDepletedEvent also dispatches:
   
   InventoryEventListener     - Logs low stock warning
   NotificationEventListener  - Alerts purchasing team (async)
                │
                ▼
6. Response returns to client (201 Created)
```

---

## Migration Steps

### Step 1: Create Event Infrastructure

1. Create `event/DomainEvent.java` interface
2. Create `event/EventPublisher.java` interface
3. Create `event/SpringEventPublisher.java` implementation

### Step 2: Define Domain Events

1. Create `event/order/` package with order events
2. Create `event/catalog/` package with catalog events
3. Add factory methods to create events from entities

### Step 3: Update Services to Publish Events

1. Inject `EventPublisher` into services
2. Add event publishing after state changes
3. Use `@Transactional` to ensure events publish within transaction

### Step 4: Create Event Listeners

1. Create `listener/inventory/InventoryEventListener.java`
2. Create `listener/notification/NotificationEventListener.java`
3. Create `listener/analytics/AnalyticsEventListener.java`
4. Create `listener/audit/AuditEventListener.java`

### Step 5: Configure Async Processing

1. Create `config/AsyncConfig.java` with `@EnableAsync`
2. Configure thread pool for async listeners
3. Add `@Async` to appropriate listener methods

### Step 6: Update Domain and Web Layers

1. Ensure domain entities support event creation
2. Keep web layer simple—just calls services
3. Update exception handling if needed

---

## Benefits of Event-Driven Architecture

1. **Loose Coupling**: Publishers don't know about subscribers. New features can be added by adding listeners.

2. **Single Responsibility**: Each listener handles one concern. Services focus on core business logic.

3. **Extensibility**: Adding new behavior (e.g., loyalty points) means adding a new listener—no changes to existing code.

4. **Testability**: Services can be tested without listeners. Listeners can be tested with mock events.

5. **Audit Trail**: The audit listener provides a natural audit trail of all domain events.

6. **Async Processing**: Non-critical operations (notifications, analytics) can be async without complicating service code.

---

## Notes for AI Agent

When refactoring to Event-Driven Architecture:

1. **Preserve All API Contracts**: HTTP interfaces remain identical.
2. **Events Are Immutable Records**: Use Java records for events.
3. **Events Have Factory Methods**: `EventName.from(entity)` pattern.
4. **Services Publish Events**: After successful state changes.
5. **Use @TransactionalEventListener**: For listeners that should respect transaction boundaries.
6. **Use @Async for Non-Critical**: Notifications, analytics should be async.
7. **AFTER_COMMIT Phase**: Most listeners should run after transaction commits.
8. **One Concern Per Listener**: Don't mix inventory and notification logic.
9. **EventPublisher Abstraction**: Don't inject ApplicationEventPublisher directly.
10. **Enable Async**: Add `@EnableAsync` and configure thread pool.
11. **Stock Restoration**: Cancel order listener should restore stock to products.
12. **Logging**: Log event publishing and handling for debugging.