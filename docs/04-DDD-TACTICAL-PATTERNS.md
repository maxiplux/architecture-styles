# Domain-Driven Design (DDD) Tactical Patterns

## Overview

Domain-Driven Design is an approach to software development that centers the development on the **core domain** and domain logic. DDD provides both strategic patterns (bounded contexts, context mapping) and tactical patterns (aggregates, entities, value objects, repositories).

This document focuses on the **tactical patterns** that can be applied to structure the Shopping Cart API. The key insight of DDD is that the software should reflect the business domain closely, using a **ubiquitous language** shared between developers and domain experts.

---

## Core Concepts

### Ubiquitous Language

A common vocabulary used by all team members (developers, domain experts, stakeholders) when discussing the domain. This language appears in code, documentation, and conversations.

For our Shopping Cart domain:
- **Product**: An item available for purchase in the catalog
- **Category**: A classification group for organizing products
- **Order**: A customer's intent to purchase products
- **Order Item**: A line in an order specifying product and quantity
- **Stock**: The available quantity of a product for sale

### Bounded Context

A boundary within which a particular domain model is defined and applicable. Each bounded context has its own ubiquitous language. For this project, we'll work within a single bounded context: **Shopping**.

---

## Tactical Patterns

### 1. Entities

Objects with a distinct identity that persists over time. Two entities are equal if they have the same identity, regardless of their attributes.

**Characteristics:**
- Have a unique identifier
- Identity remains constant through lifecycle
- Mutable (state can change)
- Equality based on identity, not attributes

### 2. Value Objects

Objects that describe characteristics of a thing but have no conceptual identity. Two value objects are equal if all their attributes are equal.

**Characteristics:**
- No unique identifier
- Immutable (once created, cannot be changed)
- Equality based on attributes
- Can be shared safely
- Side-effect-free behavior

### 3. Aggregates

A cluster of entities and value objects with defined boundaries. Each aggregate has a **root entity** (Aggregate Root) that is the only entry point for external access.

**Rules:**
- External objects can only reference the Aggregate Root
- The root ensures the consistency of changes within the aggregate
- Changes to the aggregate are atomic
- Aggregates are the unit of persistence

### 4. Domain Services

Operations that don't naturally belong to any entity or value object. They are stateless and operate on domain objects.

### 5. Repositories

Provide the illusion of an in-memory collection of aggregates. They encapsulate persistence logic.

**Rules:**
- One repository per Aggregate Root
- Repositories return domain objects, not persistence objects

### 6. Domain Events

Something that happened in the domain that domain experts care about. Used for communication between aggregates.

### 7. Factories

Encapsulate complex object creation logic.

---

## Target Package Structure

```
src/main/java/app/quantun/architecture/
├── ArchitectureApplication.java
│
├── catalog/                                  # BOUNDED CONTEXT: Catalog
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Category.java               # Aggregate Root
│   │   │   ├── CategoryId.java             # Value Object (typed ID)
│   │   │   ├── Product.java                # Aggregate Root
│   │   │   ├── ProductId.java              # Value Object (typed ID)
│   │   │   ├── ProductName.java            # Value Object
│   │   │   ├── Money.java                  # Value Object
│   │   │   └── Stock.java                  # Value Object
│   │   ├── repository/
│   │   │   ├── CategoryRepository.java     # Repository interface
│   │   │   └── ProductRepository.java
│   │   ├── service/
│   │   │   └── ProductDomainService.java   # Domain service (if needed)
│   │   └── event/
│   │       └── ProductStockDepletedEvent.java
│   ├── application/
│   │   ├── CategoryApplicationService.java
│   │   ├── ProductApplicationService.java
│   │   └── dto/
│   │       ├── CategoryDto.java
│   │       ├── ProductDto.java
│   │       └── ProductSearchCriteria.java
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   ├── entity/
│   │   │   │   ├── CategoryJpaEntity.java
│   │   │   │   └── ProductJpaEntity.java
│   │   │   ├── repository/
│   │   │   │   ├── CategoryJpaRepository.java
│   │   │   │   └── ProductJpaRepository.java
│   │   │   ├── adapter/
│   │   │   │   ├── CategoryRepositoryImpl.java
│   │   │   │   └── ProductRepositoryImpl.java
│   │   │   └── specification/
│   │   │       └── ProductJpaSpecifications.java
│   │   └── mapper/
│   │       ├── CategoryMapper.java
│   │       └── ProductMapper.java
│   └── api/
│       ├── CategoryController.java
│       ├── ProductController.java
│       └── dto/
│           ├── CategoryResponse.java
│           └── ProductResponse.java
│
├── ordering/                                 # BOUNDED CONTEXT: Ordering
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Order.java                  # Aggregate Root
│   │   │   ├── OrderId.java                # Value Object (typed ID)
│   │   │   ├── OrderItem.java              # Entity (within Order aggregate)
│   │   │   ├── OrderItemId.java            # Value Object
│   │   │   ├── OrderStatus.java            # Value Object (enum)
│   │   │   ├── CustomerId.java             # Value Object
│   │   │   ├── ShippingAddress.java        # Value Object
│   │   │   ├── OrderTotal.java             # Value Object
│   │   │   └── Quantity.java               # Value Object
│   │   ├── repository/
│   │   │   └── OrderRepository.java
│   │   ├── service/
│   │   │   └── OrderDomainService.java
│   │   ├── event/
│   │   │   ├── OrderCreatedEvent.java
│   │   │   └── OrderCancelledEvent.java
│   │   └── exception/
│   │       ├── InsufficientStockException.java
│   │       └── InvalidOrderException.java
│   ├── application/
│   │   ├── OrderApplicationService.java
│   │   ├── command/
│   │   │   ├── CreateOrderCommand.java
│   │   │   └── OrderItemCommand.java
│   │   └── dto/
│   │       ├── OrderDto.java
│   │       └── OrderItemDto.java
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   ├── entity/
│   │   │   │   ├── OrderJpaEntity.java
│   │   │   │   └── OrderItemJpaEntity.java
│   │   │   ├── repository/
│   │   │   │   └── OrderJpaRepository.java
│   │   │   └── adapter/
│   │   │       └── OrderRepositoryImpl.java
│   │   ├── mapper/
│   │   │   └── OrderMapper.java
│   │   └── event/
│   │       └── SpringOrderEventPublisher.java
│   └── api/
│       ├── OrderController.java
│       └── dto/
│           ├── OrderCreateRequest.java
│           ├── OrderItemRequest.java
│           ├── ShippingAddressRequest.java
│           └── OrderResponse.java
│
└── shared/                                   # SHARED KERNEL
    ├── domain/
    │   ├── AggregateRoot.java               # Base class for aggregates
    │   ├── DomainEvent.java                 # Marker interface
    │   ├── Entity.java                      # Base class for entities
    │   └── ValueObject.java                 # Base class for value objects
    ├── infrastructure/
    │   ├── config/
    │   │   ├── OpenApiConfig.java
    │   │   └── DataInitializer.java
    │   └── web/
    │       └── GlobalExceptionHandler.java
    └── application/
        └── DomainEventPublisher.java
```

---

## Domain Model Implementation

### Base Classes (Shared Kernel)

```java
// shared/domain/Entity.java
public abstract class Entity<ID> {
    
    public abstract ID getId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return getId() != null && getId().equals(entity.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
```

```java
// shared/domain/AggregateRoot.java
public abstract class AggregateRoot<ID> extends Entity<ID> {
    
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

```java
// shared/domain/ValueObject.java
public abstract class ValueObject {
    
    @Override
    public abstract boolean equals(Object o);
    
    @Override
    public abstract int hashCode();
}
```

```java
// shared/domain/DomainEvent.java
public interface DomainEvent {
    OffsetDateTime occurredOn();
}
```

### Value Objects

```java
// catalog/domain/model/Money.java
// A classic DDD value object representing monetary values
public final class Money extends ValueObject {
    
    private final BigDecimal amount;
    private final Currency currency;

    // Default currency for this application
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");

    private Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        // Ensure consistent scale
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    // Factory methods
    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount), DEFAULT_CURRENCY);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    }

    // Business operations (return new instances - immutable)
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot operate on different currencies: " + this.currency + " vs " + other.currency
            );
        }
    }

    // Getters
    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency.getSymbol() + amount.toString();
    }
}
```

```java
// catalog/domain/model/Stock.java
public final class Stock extends ValueObject {
    
    private final int quantity;

    private Stock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    public static Stock of(int quantity) {
        return new Stock(quantity);
    }

    public static Stock zero() {
        return new Stock(0);
    }

    // Business operations
    public boolean hasAvailable(int requestedQuantity) {
        return this.quantity >= requestedQuantity;
    }

    public Stock decrease(int amount) {
        if (!hasAvailable(amount)) {
            throw new IllegalStateException(
                "Cannot decrease stock by " + amount + ". Current quantity: " + quantity
            );
        }
        return new Stock(this.quantity - amount);
    }

    public Stock increase(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot increase stock by negative amount");
        }
        return new Stock(this.quantity + amount);
    }

    public boolean isEmpty() {
        return this.quantity == 0;
    }

    public int getValue() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return quantity == stock.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity);
    }

    @Override
    public String toString() {
        return String.valueOf(quantity);
    }
}
```

```java
// catalog/domain/model/ProductId.java
// Typed identifier - prevents mixing different ID types
public final class ProductId extends ValueObject {
    
    private final Long value;

    private ProductId(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        this.value = value;
    }

    public static ProductId of(Long value) {
        return new ProductId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return value.equals(productId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
```

```java
// ordering/domain/model/ShippingAddress.java
public final class ShippingAddress extends ValueObject {
    
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;

    private ShippingAddress(String street, String city, String state, 
                            String zipCode, String country) {
        // Validate required fields
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
        
        this.street = street.trim();
        this.city = city.trim();
        this.state = state != null ? state.trim() : null;
        this.zipCode = zipCode.trim();
        this.country = country.trim();
    }

    public static ShippingAddress of(String street, String city, String state, 
                                      String zipCode, String country) {
        return new ShippingAddress(street, city, state, zipCode, country);
    }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getCountry() { return country; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShippingAddress that = (ShippingAddress) o;
        return street.equals(that.street) &&
               city.equals(that.city) &&
               Objects.equals(state, that.state) &&
               zipCode.equals(that.zipCode) &&
               country.equals(that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, zipCode, country);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s %s, %s", 
            street, city, state != null ? state : "", zipCode, country);
    }
}
```

```java
// ordering/domain/model/Quantity.java
public final class Quantity extends ValueObject {
    
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 99;
    
    private final int value;

    private Quantity(int value) {
        if (value < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY);
        }
        if (value > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity cannot exceed " + MAX_QUANTITY);
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public int getValue() {
        return value;
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
```

### Aggregate Roots

```java
// catalog/domain/model/Category.java
public class Category extends AggregateRoot<CategoryId> {
    
    private final CategoryId id;
    private String name;
    private String description;
    private boolean active;
    private final OffsetDateTime createdAt;

    // Private constructor - use factory methods
    private Category(CategoryId id, String name, String description, 
                     boolean active, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Factory method for creating new categories
    public static Category create(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        return new Category(null, name, description, true, OffsetDateTime.now());
    }

    // Factory method for reconstituting from persistence
    public static Category reconstitute(CategoryId id, String name, String description, 
                                         boolean active, OffsetDateTime createdAt) {
        return new Category(id, name, description, active, createdAt);
    }

    // Business operations
    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        this.name = newName;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // Getters
    @Override
    public CategoryId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    // For persistence to assign ID
    public Category withId(CategoryId id) {
        return new Category(id, this.name, this.description, this.active, this.createdAt);
    }
}
```

```java
// catalog/domain/model/Product.java
public class Product extends AggregateRoot<ProductId> {
    
    private final ProductId id;
    private String name;
    private String description;
    private Money price;
    private Stock stock;
    private String imageUrl;
    private boolean active;
    private final CategoryId categoryId;
    private String categoryName; // Denormalized for convenience

    private Product(ProductId id, String name, String description, Money price, 
                    Stock stock, String imageUrl, boolean active, 
                    CategoryId categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.active = active;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Factory for new products
    public static Product create(String name, String description, Money price, 
                                  Stock stock, String imageUrl, 
                                  CategoryId categoryId, String categoryName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || !price.isPositive()) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category is required");
        }
        return new Product(null, name, description, price, stock, imageUrl, 
                          true, categoryId, categoryName);
    }

    // Factory for reconstitution
    public static Product reconstitute(ProductId id, String name, String description, 
                                        Money price, Stock stock, String imageUrl, 
                                        boolean active, CategoryId categoryId, 
                                        String categoryName) {
        return new Product(id, name, description, price, stock, imageUrl, 
                          active, categoryId, categoryName);
    }

    // Business operations
    public boolean isAvailable() {
        return active && !stock.isEmpty();
    }

    public boolean canFulfill(Quantity quantity) {
        return active && stock.hasAvailable(quantity.getValue());
    }

    public void decreaseStock(Quantity quantity) {
        if (!canFulfill(quantity)) {
            throw new InsufficientStockException(
                "Product " + id + " cannot fulfill quantity " + quantity + 
                ". Available: " + stock.getValue()
            );
        }
        this.stock = stock.decrease(quantity.getValue());
        
        // Register domain event if stock is depleted
        if (stock.isEmpty()) {
            registerEvent(new ProductStockDepletedEvent(this.id, OffsetDateTime.now()));
        }
    }

    public void increaseStock(Quantity quantity) {
        this.stock = stock.increase(quantity.getValue());
    }

    public void updatePrice(Money newPrice) {
        if (newPrice == null || !newPrice.isPositive()) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = newPrice;
    }

    public void deactivate() {
        this.active = false;
    }

    // Getters
    @Override
    public ProductId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public Stock getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public CategoryId getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    public Product withId(ProductId id) {
        return new Product(id, name, description, price, stock, imageUrl, 
                          active, categoryId, categoryName);
    }
}
```

```java
// ordering/domain/model/Order.java
public class Order extends AggregateRoot<OrderId> {
    
    private OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final Money subtotal;
    private final Money tax;
    private final Money total;
    private final OffsetDateTime createdAt;
    private final ShippingAddress shippingAddress;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private Order(OrderId id, CustomerId customerId, OrderStatus status, 
                  List<OrderItem> items, Money subtotal, Money tax, Money total,
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

    // Factory for creating new orders
    public static Order create(CustomerId customerId, List<OrderItem> items, 
                                ShippingAddress shippingAddress) {
        if (customerId == null) {
            throw new InvalidOrderException("Customer ID is required");
        }
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must have at least one item");
        }
        if (shippingAddress == null) {
            throw new InvalidOrderException("Shipping address is required");
        }

        Money subtotal = calculateSubtotal(items);
        Money tax = subtotal.multiply(TAX_RATE);
        Money total = subtotal.add(tax);

        Order order = new Order(
            null,
            customerId,
            OrderStatus.PENDING,
            new ArrayList<>(items),
            subtotal,
            tax,
            total,
            OffsetDateTime.now(),
            shippingAddress
        );

        // Register domain event
        order.registerEvent(new OrderCreatedEvent(
            order.id, 
            order.customerId, 
            order.total,
            OffsetDateTime.now()
        ));

        return order;
    }

    // Factory for reconstitution from persistence
    public static Order reconstitute(OrderId id, CustomerId customerId, OrderStatus status,
                                      List<OrderItem> items, Money subtotal, Money tax, 
                                      Money total, OffsetDateTime createdAt,
                                      ShippingAddress shippingAddress) {
        return new Order(id, customerId, status, items, subtotal, tax, total, 
                        createdAt, shippingAddress);
    }

    private static Money calculateSubtotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.zero(), Money::add);
    }

    // Business operations
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel shipped or delivered orders");
        }
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelledEvent(this.id, OffsetDateTime.now()));
    }

    // Getters
    @Override
    public OrderId getId() { return id; }
    public CustomerId getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Money getSubtotal() { return subtotal; }
    public Money getTax() { return tax; }
    public Money getTotal() { return total; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }

    public void assignId(OrderId id) {
        if (this.id != null) {
            throw new IllegalStateException("Order already has an ID");
        }
        this.id = id;
    }
}
```

```java
// ordering/domain/model/OrderItem.java
// Entity within the Order aggregate (not an Aggregate Root)
public class OrderItem extends Entity<OrderItemId> {
    
    private final OrderItemId id;
    private final ProductId productId;
    private final String productName;
    private final Money unitPrice;
    private final Quantity quantity;
    private final Money subtotal;

    private OrderItem(OrderItemId id, ProductId productId, String productName,
                      Money unitPrice, Quantity quantity, Money subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    // Factory for new order items
    public static OrderItem create(ProductId productId, String productName,
                                    Money unitPrice, Quantity quantity) {
        Money subtotal = unitPrice.multiply(quantity.getValue());
        return new OrderItem(null, productId, productName, unitPrice, quantity, subtotal);
    }

    // Factory for reconstitution
    public static OrderItem reconstitute(OrderItemId id, ProductId productId, 
                                          String productName, Money unitPrice,
                                          Quantity quantity, Money subtotal) {
        return new OrderItem(id, productId, productName, unitPrice, quantity, subtotal);
    }

    // Getters
    @Override
    public OrderItemId getId() { return id; }
    public ProductId getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Money getUnitPrice() { return unitPrice; }
    public Quantity getQuantity() { return quantity; }
    public Money getSubtotal() { return subtotal; }
}
```

### Domain Events

```java
// ordering/domain/event/OrderCreatedEvent.java
public record OrderCreatedEvent(
    OrderId orderId,
    CustomerId customerId,
    Money total,
    OffsetDateTime occurredOn
) implements DomainEvent {}
```

```java
// catalog/domain/event/ProductStockDepletedEvent.java
public record ProductStockDepletedEvent(
    ProductId productId,
    OffsetDateTime occurredOn
) implements DomainEvent {}
```

### Repositories (Domain Interfaces)

```java
// catalog/domain/repository/ProductRepository.java
// Repository interface in domain layer - implementation in infrastructure
public interface ProductRepository {
    
    Optional<Product> findById(ProductId id);
    
    List<Product> findAllByIds(List<ProductId> ids);
    
    Page<Product> findByCriteria(ProductSearchCriteria criteria, int page, int size, 
                                  String sortBy, String sortDirection);
    
    Product save(Product product);
    
    void delete(ProductId id);
}
```

```java
// ordering/domain/repository/OrderRepository.java
public interface OrderRepository {
    
    Optional<Order> findById(OrderId id);
    
    Order save(Order order);
    
    List<Order> findByCustomerId(CustomerId customerId);
}
```

### Application Services

```java
// ordering/application/OrderApplicationService.java
@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public OrderDto createOrder(CreateOrderCommand command) {
        // Load products
        List<ProductId> productIds = command.items().stream()
            .map(item -> ProductId.of(item.productId()))
            .toList();
        
        List<Product> products = productRepository.findAllByIds(productIds);
        
        if (products.size() != productIds.size()) {
            throw new EntityNotFoundException("One or more products not found");
        }

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemCommand itemCmd : command.items()) {
            Product product = products.stream()
                .filter(p -> p.getId().getValue().equals(itemCmd.productId()))
                .findFirst()
                .orElseThrow();

            // Validate product can fulfill order
            Quantity quantity = Quantity.of(itemCmd.quantity());
            if (!product.canFulfill(quantity)) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getId()
                );
            }

            OrderItem orderItem = OrderItem.create(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity
            );
            orderItems.add(orderItem);
        }

        // Create shipping address
        ShippingAddress shippingAddress = ShippingAddress.of(
            command.shippingStreet(),
            command.shippingCity(),
            command.shippingState(),
            command.shippingZipCode(),
            command.shippingCountry()
        );

        // Create order (domain logic calculates totals)
        CustomerId customerId = CustomerId.of(command.customerId());
        Order order = Order.create(customerId, orderItems, shippingAddress);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Decrease stock for each product
        for (OrderItem item : savedOrder.getItems()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(item.getProductId()))
                .findFirst()
                .orElseThrow();
            
            product.decreaseStock(item.getQuantity());
            productRepository.save(product);
        }

        // Publish domain events
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();

        return toDto(savedOrder);
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
            .map(item -> new OrderItemDto(
                item.getProductId().getValue(),
                item.getProductName(),
                item.getQuantity().getValue(),
                item.getUnitPrice().getAmount(),
                item.getSubtotal().getAmount()
            ))
            .toList();

        return new OrderDto(
            order.getId().getValue(),
            order.getStatus().name(),
            items,
            order.getSubtotal().getAmount(),
            order.getTax().getAmount(),
            order.getTotal().getAmount(),
            order.getCreatedAt()
        );
    }
}
```

### Event Publisher

```java
// shared/application/DomainEventPublisher.java
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
```

```java
// ordering/infrastructure/event/SpringOrderEventPublisher.java
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

```java
// Example listener in another bounded context
@Component
@Slf4j
public class OrderEventListener {
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: {} for customer {} with total {}", 
            event.orderId(), event.customerId(), event.total());
        // Could trigger notifications, analytics, etc.
    }
}
```

---

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API LAYER                                       │
│                                                                              │
│   catalog/api/                          ordering/api/                        │
│   - CategoryController                  - OrderController                    │
│   - ProductController                   - DTOs                               │
│   - DTOs                                                                     │
│                                                                              │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │ uses
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          APPLICATION LAYER                                   │
│                                                                              │
│   catalog/application/                  ordering/application/                │
│   - CategoryApplicationService          - OrderApplicationService            │
│   - ProductApplicationService           - CreateOrderCommand                 │
│   - DTOs                                - DTOs                               │
│                                                                              │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │ uses
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DOMAIN LAYER                                       │
│                                                                              │
│   catalog/domain/                       ordering/domain/                     │
│   ┌─────────────────────────┐          ┌─────────────────────────┐          │
│   │ model/                  │          │ model/                  │          │
│   │ - Category (AR)         │          │ - Order (AR)            │          │
│   │ - Product (AR)          │          │ - OrderItem (Entity)    │          │
│   │ - Money (VO)            │          │ - ShippingAddress (VO)  │          │
│   │ - Stock (VO)            │          │ - Quantity (VO)         │          │
│   │ - ProductId (VO)        │          │ - OrderId (VO)          │          │
│   └─────────────────────────┘          └─────────────────────────┘          │
│                                                                              │
│   repository/ (interfaces)              repository/ (interfaces)             │
│   - CategoryRepository                  - OrderRepository                    │
│   - ProductRepository                                                        │
│                                                                              │
│   event/                                event/                               │
│   - ProductStockDepletedEvent          - OrderCreatedEvent                   │
│                                        - OrderCancelledEvent                 │
│                                                                              │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │ implemented by
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE LAYER                                  │
│                                                                              │
│   catalog/infrastructure/               ordering/infrastructure/             │
│   - JPA Entities                        - JPA Entities                       │
│   - JPA Repositories                    - JPA Repositories                   │
│   - Repository Implementations          - Repository Implementations         │
│   - Mappers                             - Mappers                            │
│   - Specifications                      - Event Publisher                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## MapStruct Integration

Domain-Driven Design has clear transformation boundaries between the API layer, application layer, domain layer, and infrastructure layer. MapStruct helps maintain the purity of your domain while efficiently handling these transformations. The key principle is that your domain objects should never be polluted with mapping annotations—all mapping logic stays in the infrastructure or application layers.

### Adding MapStruct Dependency

```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

### API Layer Mappers (HTTP ↔ Application DTOs)

These mappers transform between HTTP request/response models and application layer DTOs.

```java
// catalog/api/mapper/ProductApiMapper.java
@Mapper(componentModel = "spring")
public interface ProductApiMapper {
    
    // Application DTO → API Response
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "price", target = "price")
    ProductResponse toResponse(ProductDto dto);
    
    List<ProductResponse> toResponseList(List<ProductDto> dtos);
    
    // API Request → Application DTO
    ProductSearchCriteria toCriteria(ProductFilterRequest request);
}
```

```java
// ordering/api/mapper/OrderApiMapper.java
@Mapper(componentModel = "spring", uses = {OrderItemApiMapper.class})
public interface OrderApiMapper {
    
    // API Request → Application Command
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "items", target = "items")
    @Mapping(source = "shippingAddress.street", target = "shippingStreet")
    @Mapping(source = "shippingAddress.city", target = "shippingCity")
    @Mapping(source = "shippingAddress.state", target = "shippingState")
    @Mapping(source = "shippingAddress.zipCode", target = "shippingZipCode")
    @Mapping(source = "shippingAddress.country", target = "shippingCountry")
    CreateOrderCommand toCommand(OrderCreateRequest request);
    
    // Application DTO → API Response
    OrderResponse toResponse(OrderDto dto);
}
```

### Application Layer Mappers (Domain ↔ DTOs)

These mappers handle the transformation between rich domain objects and simple data transfer objects used by the application services.

```java
// catalog/application/mapper/ProductApplicationMapper.java
@Mapper(componentModel = "spring")
public interface ProductApplicationMapper {
    
    // Domain → DTO
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "price.amount", target = "price")
    @Mapping(source = "stock.value", target = "stock")
    @Mapping(source = "category.id.value", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDto toDto(Product product);
    
    List<ProductDto> toDtoList(List<Product> products);
}
```

```java
// ordering/application/mapper/OrderApplicationMapper.java
@Mapper(componentModel = "spring", uses = {OrderItemApplicationMapper.class})
public interface OrderApplicationMapper {
    
    // Domain → DTO
    @Mapping(source = "id.value", target = "orderId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "subtotal.amount", target = "subtotal")
    @Mapping(source = "tax.amount", target = "tax")
    @Mapping(source = "total.amount", target = "total")
    @Mapping(source = "items", target = "items")
    OrderDto toDto(Order order);
}
```

```java
// ordering/application/mapper/OrderItemApplicationMapper.java
@Mapper(componentModel = "spring")
public interface OrderItemApplicationMapper {
    
    @Mapping(source = "productId.value", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "quantity.value", target = "quantity")
    @Mapping(source = "unitPrice.amount", target = "unitPrice")
    @Mapping(source = "subtotal.amount", target = "subtotal")
    OrderItemDto toDto(OrderItem orderItem);
}
```

### Infrastructure Mappers (Domain ↔ JPA Entities)

These mappers are crucial in DDD—they keep your domain pure by isolating all JPA concerns to the infrastructure layer.

```java
// catalog/infrastructure/mapper/ProductInfrastructureMapper.java
@Mapper(componentModel = "spring", uses = {CategoryInfrastructureMapper.class})
public interface ProductInfrastructureMapper {
    
    // JPA Entity → Domain Aggregate
    // This is complex because we're reconstituting value objects from primitives
    default Product toDomain(ProductJpaEntity entity) {
        CategoryId categoryId = CategoryId.of(entity.getCategory().getId());
        Category category = Category.reconstitute(
            categoryId,
            entity.getCategory().getName(),
            entity.getCategory().getDescription(),
            entity.getCategory().isActive(),
            entity.getCategory().getCreatedAt()
        );
        
        return Product.reconstitute(
            ProductId.of(entity.getId()),
            entity.getName(),
            entity.getDescription(),
            Money.of(entity.getPrice()),
            Stock.of(entity.getStock()),
            entity.getImageUrl(),
            entity.isActive(),
            categoryId,
            category.getName()
        );
    }
    
    List<Product> toDomainList(List<ProductJpaEntity> entities);
    
    // Domain Aggregate → JPA Entity
    default ProductJpaEntity toEntity(Product product) {
        CategoryJpaEntity categoryEntity = CategoryJpaEntity.builder()
            .id(product.getCategoryId().getValue())
            .build();
        
        return ProductJpaEntity.builder()
            .id(product.getId() != null ? product.getId().getValue() : null)
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice().getAmount())
            .stock(product.getStock().getValue())
            .imageUrl(product.getImageUrl())
            .active(product.isActive())
            .category(categoryEntity)
            .build();
    }
}
```

```java
// ordering/infrastructure/mapper/OrderInfrastructureMapper.java
@Mapper(componentModel = "spring", uses = {OrderItemInfrastructureMapper.class})
public interface OrderInfrastructureMapper {
    
    // JPA Entity → Domain Aggregate
    // Complex mapping that reconstructs value objects and entities
    default Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
            .map(this::itemToDomain)
            .toList();
        
        ShippingAddress shippingAddress = ShippingAddress.of(
            entity.getShippingStreet(),
            entity.getShippingCity(),
            entity.getShippingState(),
            entity.getShippingZipCode(),
            entity.getShippingCountry()
        );
        
        return Order.reconstitute(
            OrderId.of(entity.getId()),
            CustomerId.of(entity.getCustomerId()),
            entity.getStatus(),
            items,
            Money.of(entity.getSubtotal()),
            Money.of(entity.getTax()),
            Money.of(entity.getTotal()),
            entity.getCreatedAt(),
            shippingAddress
        );
    }
    
    // Helper method for mapping order items
    default OrderItem itemToDomain(OrderItemJpaEntity entity) {
        return OrderItem.reconstitute(
            OrderItemId.of(entity.getId()),
            ProductId.of(entity.getProduct().getId()),
            entity.getProduct().getName(),
            Money.of(entity.getUnitPrice()),
            Quantity.of(entity.getQuantity()),
            Money.of(entity.getSubtotal())
        );
    }
    
    // Domain Aggregate → JPA Entity
    default OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.builder()
            .id(order.getId() != null ? order.getId().getValue() : null)
            .customerId(order.getCustomerId().getValue())
            .status(order.getStatus())
            .subtotal(order.getSubtotal().getAmount())
            .tax(order.getTax().getAmount())
            .total(order.getTotal().getAmount())
            .shippingStreet(order.getShippingAddress().getStreet())
            .shippingCity(order.getShippingAddress().getCity())
            .shippingState(order.getShippingAddress().getState())
            .shippingZipCode(order.getShippingAddress().getZipCode())
            .shippingCountry(order.getShippingAddress().getCountry())
            .createdAt(order.getCreatedAt())
            .build();
        
        // Map items and set bidirectional relationship
        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
            .map(item -> itemToEntity(item, entity))
            .toList();
        
        entity.setItems(itemEntities);
        return entity;
    }
    
    default OrderItemJpaEntity itemToEntity(OrderItem item, OrderJpaEntity order) {
        ProductJpaEntity product = ProductJpaEntity.builder()
            .id(item.getProductId().getValue())
            .name(item.getProductName())
            .build();
        
        return OrderItemJpaEntity.builder()
            .id(item.getId() != null ? item.getId().getValue() : null)
            .order(order)
            .product(product)
            .quantity(item.getQuantity().getValue())
            .unitPrice(item.getUnitPrice().getAmount())
            .subtotal(item.getSubtotal().getAmount())
            .build();
    }
}
```

---

## Testing Strategy

DDD's rich domain model enables highly focused testing. Your value objects and entities contain critical business logic that must be thoroughly tested, but because they're free of infrastructure concerns, these tests are fast and straightforward. Application services orchestrate domain objects, and their tests focus on this orchestration. Infrastructure tests verify that your persistence layer correctly reconstitutes aggregates from storage.

### Test Dependencies

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

### Value Object Tests

Value objects contain business rules and validations. They're immutable and test themselves through their constructors and methods. These are your fastest, most focused tests.

```java
// catalog/domain/model/MoneyTest.java
class MoneyTest {
    
    @Test
    void of_withValidAmount_createsMoney() {
        // When
        Money money = Money.of(new BigDecimal("99.99"));
        
        // Then
        assertEquals(new BigDecimal("99.99"), money.getAmount());
        assertEquals(Currency.getInstance("USD"), money.getCurrency());
    }
    
    @Test
    void of_withNullAmount_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            Money.of(null)
        );
    }
    
    @Test
    void add_withSameCurrency_addsCorrectly() {
        // Given
        Money money1 = Money.of(new BigDecimal("10.50"));
        Money money2 = Money.of(new BigDecimal("5.25"));
        
        // When
        Money result = money1.add(money2);
        
        // Then
        assertEquals(new BigDecimal("15.75"), result.getAmount());
    }
    
    @Test
    void multiply_byQuantity_calculatesCorrectly() {
        // Given
        Money unitPrice = Money.of(new BigDecimal("25.00"));
        
        // When
        Money total = unitPrice.multiply(3);
        
        // Then
        assertEquals(new BigDecimal("75.00"), total.getAmount());
    }
    
    @Test
    void add_withDifferentCurrency_throwsException() {
        // Given
        Money usd = Money.of(new BigDecimal("10.00"), Currency.getInstance("USD"));
        Money eur = Money.of(new BigDecimal("10.00"), Currency.getInstance("EUR"));
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
    }
}
```

```java
// catalog/domain/model/StockTest.java
class StockTest {
    
    @Test
    void of_withValidQuantity_createsStock() {
        // When
        Stock stock = Stock.of(10);
        
        // Then
        assertEquals(10, stock.getValue());
    }
    
    @Test
    void of_withNegativeQuantity_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Stock.of(-5));
    }
    
    @Test
    void hasAvailable_withSufficientStock_returnsTrue() {
        // Given
        Stock stock = Stock.of(10);
        
        // Then
        assertTrue(stock.hasAvailable(5));
        assertTrue(stock.hasAvailable(10));
        assertFalse(stock.hasAvailable(11));
    }
    
    @Test
    void decrease_withSufficientStock_decreasesCorrectly() {
        // Given
        Stock stock = Stock.of(10);
        
        // When
        Stock decreased = stock.decrease(3);
        
        // Then
        assertEquals(7, decreased.getValue());
        assertEquals(10, stock.getValue()); // Original unchanged (immutable)
    }
    
    @Test
    void decrease_withInsufficientStock_throwsException() {
        // Given
        Stock stock = Stock.of(5);
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> stock.decrease(10)
        );
        assertTrue(exception.getMessage().contains("Cannot decrease stock"));
    }
}
```

```java
// ordering/domain/model/QuantityTest.java
class QuantityTest {
    
    @Test
    void of_withValidQuantity_createsQuantity() {
        // When
        Quantity quantity = Quantity.of(5);
        
        // Then
        assertEquals(5, quantity.getValue());
    }
    
    @Test
    void of_withTooLow_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(0));
    }
    
    @Test
    void of_withTooHigh_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(100));
    }
}
```

### Aggregate Root Tests

Aggregates are the heart of DDD—they enforce business invariants and consistency boundaries. These tests verify that aggregates maintain their invariants under all circumstances.

```java
// catalog/domain/model/ProductTest.java
class ProductTest {
    
    @Test
    void create_withValidData_createsProduct() {
        // Given
        CategoryId categoryId = CategoryId.of(1L);
        
        // When
        Product product = Product.create(
            "Gaming Laptop",
            "High-performance laptop",
            Money.of(new BigDecimal("1299.99")),
            Stock.of(10),
            "laptop.jpg",
            categoryId,
            "Electronics"
        );
        
        // Then
        assertNotNull(product);
        assertEquals("Gaming Laptop", product.getName());
        assertEquals(10, product.getStock().getValue());
        assertTrue(product.isActive());
    }
    
    @Test
    void create_withNullName_throwsException() {
        // Given
        CategoryId categoryId = CategoryId.of(1L);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            Product.create(null, "Description", Money.of(BigDecimal.TEN), 
                          Stock.of(5), "img.jpg", categoryId, "Electronics")
        );
    }
    
    @Test
    void decreaseStock_withSufficientStock_decreasesAndMaintainsInvariant() {
        // Given
        CategoryId categoryId = CategoryId.of(1L);
        Product product = Product.create(
            "Laptop", "Desc", Money.of(BigDecimal.TEN),
            Stock.of(10), "img.jpg", categoryId, "Electronics"
        );
        Quantity quantity = Quantity.of(3);
        
        // When
        product.decreaseStock(quantity);
        
        // Then
        assertEquals(7, product.getStock().getValue());
    }
    
    @Test
    void decreaseStock_withInsufficientStock_throwsDomainException() {
        // Given
        CategoryId categoryId = CategoryId.of(1L);
        Product product = Product.create(
            "Laptop", "Desc", Money.of(BigDecimal.TEN),
            Stock.of(2), "img.jpg", categoryId, "Electronics"
        );
        Quantity quantity = Quantity.of(5);
        
        // When & Then
        assertThrows(InsufficientStockException.class, () ->
            product.decreaseStock(quantity)
        );
    }
    
    @Test
    void canFulfill_withVariousQuantities_returnsCorrectly() {
        // Given
        CategoryId categoryId = CategoryId.of(1L);
        Product product = Product.create(
            "Laptop", "Desc", Money.of(BigDecimal.TEN),
            Stock.of(5), "img.jpg", categoryId, "Electronics"
        );
        
        // Then
        assertTrue(product.canFulfill(Quantity.of(5)));
        assertTrue(product.canFulfill(Quantity.of(3)));
        assertFalse(product.canFulfill(Quantity.of(6)));
    }
}
```

```java
// ordering/domain/model/OrderTest.java
class OrderTest {
    
    @Test
    void create_withValidData_createsOrderWithCorrectTotals() {
        // Given
        OrderItem item1 = OrderItem.create(
            ProductId.of(1L),
            "Laptop",
            Money.of(new BigDecimal("1000.00")),
            Quantity.of(2)
        );
        OrderItem item2 = OrderItem.create(
            ProductId.of(2L),
            "Mouse",
            Money.of(new BigDecimal("50.00")),
            Quantity.of(1)
        );
        
        ShippingAddress address = ShippingAddress.of(
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        // When
        Order order = Order.create(
            CustomerId.of(1L),
            List.of(item1, item2),
            address
        );
        
        // Then
        assertEquals(new BigDecimal("2050.00"), order.getSubtotal().getAmount());
        assertEquals(new BigDecimal("164.00"), order.getTax().getAmount()); // 8%
        assertEquals(new BigDecimal("2214.00"), order.getTotal().getAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        
        // Verify domain event was registered
        assertEquals(1, order.getDomainEvents().size());
        assertTrue(order.getDomainEvents().get(0) instanceof OrderCreatedEvent);
    }
    
    @Test
    void create_withEmptyItems_throwsException() {
        // Given
        ShippingAddress address = ShippingAddress.of(
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        // When & Then
        assertThrows(InvalidOrderException.class, () ->
            Order.create(CustomerId.of(1L), List.of(), address)
        );
    }
    
    @Test
    void confirm_fromPendingStatus_changesStatusToConfirmed() {
        // Given
        OrderItem item = OrderItem.create(
            ProductId.of(1L), "Laptop",
            Money.of(new BigDecimal("1000.00")), Quantity.of(1)
        );
        ShippingAddress address = ShippingAddress.of(
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        Order order = Order.create(CustomerId.of(1L), List.of(item), address);
        
        // When
        order.confirm();
        
        // Then
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }
    
    @Test
    void cancel_fromShippedStatus_throwsException() {
        // Given - create order and move to shipped state
        OrderItem item = OrderItem.create(
            ProductId.of(1L), "Laptop",
            Money.of(new BigDecimal("1000.00")), Quantity.of(1)
        );
        ShippingAddress address = ShippingAddress.of(
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        Order order = Order.create(CustomerId.of(1L), List.of(item), address);
        order.confirm();
        order.ship();
        
        // When & Then - cannot cancel shipped order
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }
}
```

### Application Service Tests

Application services orchestrate domain objects and coordinate with repositories. Mock the repositories to focus on orchestration logic.

```java
// ordering/application/OrderApplicationServiceTest.java
@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @Mock
    private OrderApplicationMapper mapper;
    
    private OrderApplicationService service;
    
    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
            orderRepository, productRepository, mapper, eventPublisher
        );
    }
    
    @Test
    void createOrder_withValidCommand_createsOrderAndPublishesEvent() {
        // Given
        CategoryId categoryId = CategoryId.of(1L);
        Product product = Product.create(
            "Laptop", "Gaming laptop",
            Money.of(new BigDecimal("1000.00")),
            Stock.of(10),
            "img.jpg",
            categoryId,
            "Electronics"
        );
        
        CreateOrderCommand command = new CreateOrderCommand(
            1L,
            List.of(new OrderItemCommand(1L, 2)),
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        // Configure mocks
        when(productRepository.findAllByIds(anyList()))
            .thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(inv -> {
                Order order = inv.getArgument(0);
                order.assignId(OrderId.of(100L));
                return order;
            });
        when(mapper.toDto(any(Order.class)))
            .thenReturn(new OrderDto(100L, "PENDING", List.of(), 
                                     BigDecimal.ZERO, BigDecimal.ZERO, 
                                     BigDecimal.ZERO, OffsetDateTime.now()));
        
        // When
        OrderDto result = service.createOrder(command);
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.orderId());
        
        // Verify domain event was published
        verify(eventPublisher).publish(any(OrderCreatedEvent.class));
        
        // Verify stock was decreased
        verify(productRepository).save(argThat(p -> 
            p.getStock().getValue() == 8
        ));
    }
    
    @Test
    void createOrder_withInsufficientStock_throwsException() {
        // Given
        CategoryId categoryId = CategoryId.of(1L);
        Product product = Product.create(
            "Laptop", "Gaming laptop",
            Money.of(new BigDecimal("1000.00")),
            Stock.of(1), // Only 1 in stock
            "img.jpg",
            categoryId,
            "Electronics"
        );
        
        CreateOrderCommand command = new CreateOrderCommand(
            1L,
            List.of(new OrderItemCommand(1L, 5)), // Requesting 5
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        when(productRepository.findAllByIds(anyList()))
            .thenReturn(List.of(product));
        
        // When & Then
        assertThrows(InsufficientStockException.class, () ->
            service.createOrder(command)
        );
        
        // Verify order was never saved
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
```

### Repository Implementation Tests

These integration tests verify that your infrastructure correctly reconstitutes aggregates from persistence and maintains aggregate boundaries.

```java
// catalog/infrastructure/persistence/adapter/ProductRepositoryImplTest.java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ProductRepositoryImpl.class, ProductInfrastructureMapperImpl.class, 
         CategoryInfrastructureMapperImpl.class})
class ProductRepositoryImplTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private ProductRepositoryImpl repository;
    
    @Autowired
    private ProductJpaRepository jpaRepository;
    
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    
    @Test
    void save_newProduct_persistsAndReconstitutesCorrectly() {
        // Given - create domain aggregate with value objects
        CategoryJpaEntity categoryEntity = categoryJpaRepository.save(
            CategoryJpaEntity.builder()
                .name("Electronics")
                .description("Electronic devices")
                .active(true)
                .build()
        );
        
        CategoryId categoryId = CategoryId.of(categoryEntity.getId());
        Product product = Product.create(
            "Gaming Mouse",
            "RGB gaming mouse",
            Money.of(new BigDecimal("79.99")),
            Stock.of(25),
            "mouse.jpg",
            categoryId,
            "Electronics"
        );
        
        // When
        Product saved = repository.save(product);
        
        // Then - verify domain object was reconstituted correctly
        assertNotNull(saved.getId());
        assertEquals("Gaming Mouse", saved.getName());
        assertEquals(new BigDecimal("79.99"), saved.getPrice().getAmount());
        assertEquals(25, saved.getStock().getValue());
        
        // Verify in database
        ProductJpaEntity entityInDb = jpaRepository.findById(
            saved.getId().getValue()
        ).orElseThrow();
        assertEquals("Gaming Mouse", entityInDb.getName());
        assertEquals(new BigDecimal("79.99"), entityInDb.getPrice());
    }
    
    @Test
    void findById_existingProduct_reconstructsAggregateWithValueObjects() {
        // Given - create test data in database
        CategoryJpaEntity categoryEntity = categoryJpaRepository.save(
            CategoryJpaEntity.builder()
                .name("Electronics")
                .active(true)
                .build()
        );
        
        ProductJpaEntity productEntity = jpaRepository.save(
            ProductJpaEntity.builder()
                .name("Laptop")
                .price(new BigDecimal("1299.99"))
                .stock(10)
                .active(true)
                .category(categoryEntity)
                .build()
        );
        
        // When
        Optional<Product> result = repository.findById(
            ProductId.of(productEntity.getId())
        );
        
        // Then - verify domain aggregate with value objects
        assertTrue(result.isPresent());
        Product product = result.get();
        assertEquals("Laptop", product.getName());
        
        // Verify value objects were properly reconstructed
        assertInstanceOf(Money.class, product.getPrice());
        assertInstanceOf(Stock.class, product.getStock());
        assertInstanceOf(ProductId.class, product.getId());
        
        assertEquals(new BigDecimal("1299.99"), product.getPrice().getAmount());
        assertEquals(10, product.getStock().getValue());
    }
}
```

### Domain Event Tests

Verify that domain events are published correctly and listeners handle them appropriately.

```java
// ordering/domain/event/OrderEventTest.java
@SpringBootTest
@Testcontainers
class OrderEventTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private OrderApplicationService orderService;
    
    @Autowired
    private ProductJpaRepository productRepository;
    
    @Autowired
    private CategoryJpaRepository categoryRepository;
    
    @Captor
    private ArgumentCaptor<OrderCreatedEvent> eventCaptor;
    
    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    @BeforeEach
    void setUp() {
        CategoryJpaEntity category = categoryRepository.save(
            CategoryJpaEntity.builder()
                .name("Electronics")
                .active(true)
                .build()
        );
        
        productRepository.save(
            ProductJpaEntity.builder()
                .name("Laptop")
                .price(new BigDecimal("1000.00"))
                .stock(10)
                .active(true)
                .category(category)
                .build()
        );
    }
    
    @Test
    void createOrder_publishesOrderCreatedEvent() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand(
            1L,
            List.of(new OrderItemCommand(1L, 2)),
            "123 Main St", "Springfield", "IL", "62701", "USA"
        );
        
        // When
        orderService.createOrder(command);
        
        // Then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderCreatedEvent event = eventCaptor.getValue();
        
        assertNotNull(event);
        assertNotNull(event.orderId());
        assertEquals(CustomerId.of(1L), event.customerId());
        assertTrue(event.total().getAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}
```

### Architecture Tests

Use ArchUnit to enforce DDD patterns and bounded context boundaries.

```java
// architecture/DDDArchitectureTest.java
class DDDArchitectureTest {
    
    private static final String BASE_PACKAGE = "app.quantun.architecture";
    private final JavaClasses classes = new ClassFileImporter()
        .importPackages(BASE_PACKAGE);
    
    @Test
    void domainModelShouldNotDependOnApplication() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain.model..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application..");
        
        rule.check(classes);
    }
    
    @Test
    void domainModelShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain.model..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "org.springframework..");
        
        rule.check(classes);
    }
    
    @Test
    void repositoriesShouldBeInterfacesInDomain() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain.repository..")
            .should().beInterfaces()
            .because("Repository interfaces belong in domain layer");
        
        rule.check(classes);
    }
    
    @Test
    void valueObjectsShouldBeImmutable() {
        ArchRule rule = classes()
            .that().areAssignableTo(ValueObject.class)
            .should().haveOnlyFinalFields()
            .because("Value objects must be immutable");
        
        rule.check(classes);
    }
    
    @Test
    void aggregateRootsShouldExtendAggregateRootBaseClass() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain.model..")
            .and().haveSimpleNameEndingWith("Order")
            .or().haveSimpleNameEndingWith("Product")
            .or().haveSimpleNameEndingWith("Category")
            .should().beAssignableTo(AggregateRoot.class);
        
        rule.check(classes);
    }
    
    @Test
    void boundedContextsShouldNotDependOnEachOther() {
        // Catalog should not depend on Ordering
        ArchRule rule1 = noClasses()
            .that().resideInAPackage("..catalog..")
            .should().dependOnClassesThat()
            .resideInAPackage("..ordering..");
        
        // Ordering should not depend on Catalog
        ArchRule rule2 = noClasses()
            .that().resideInAPackage("..ordering..")
            .should().dependOnClassesThat()
            .resideInAPackage("..catalog..");
        
        rule1.check(classes);
        rule2.check(classes);
    }
}
```

---
## Migration Steps

### Step 1: Create Shared Kernel
1. Create base classes: `Entity`, `AggregateRoot`, `ValueObject`, `DomainEvent`
2. Create shared exception types
3. Create `DomainEventPublisher` interface

### Step 2: Create Value Objects
1. Create typed IDs: `ProductId`, `CategoryId`, `OrderId`, `CustomerId`
2. Create domain value objects: `Money`, `Stock`, `Quantity`, `ShippingAddress`
3. Ensure immutability and validation in constructors

### Step 3: Create Aggregate Roots
1. Create `Category` and `Product` aggregates in `catalog/domain/model/`
2. Create `Order` aggregate with `OrderItem` entity in `ordering/domain/model/`
3. Add factory methods and business logic
4. Remove JPA annotations (these go on infrastructure entities)

### Step 4: Create Domain Events
1. Create events: `OrderCreatedEvent`, `ProductStockDepletedEvent`
2. Add event registration in aggregates

### Step 5: Create Repository Interfaces
1. Create `CategoryRepository`, `ProductRepository` in `catalog/domain/repository/`
2. Create `OrderRepository` in `ordering/domain/repository/`

### Step 6: Create Application Services
1. Create `CategoryApplicationService`, `ProductApplicationService`
2. Create `OrderApplicationService` with orchestration logic
3. Create command objects and DTOs

### Step 7: Create Infrastructure Layer
1. Create JPA entities in `infrastructure/persistence/entity/`
2. Create JPA repositories in `infrastructure/persistence/repository/`
3. Create repository implementations (adapters)
4. Create mappers between domain and JPA entities

### Step 8: Create API Layer
1. Create controllers in `api/`
2. Create request/response DTOs
3. Wire to application services

### Step 9: Update Configuration
1. Update `DataInitializer` to use application services
2. Create Spring event publisher implementation
3. Add event listeners if needed

---

## Notes for AI Agent

When refactoring to DDD Tactical Patterns:

1. **Preserve All API Contracts**: HTTP interfaces remain identical.
2. **Value Objects Are Immutable**: No setters, return new instances for modifications.
3. **Aggregates Are Transaction Boundaries**: One transaction per aggregate.
4. **Repository Per Aggregate Root**: Not per entity.
5. **Domain Has No Infrastructure Dependencies**: No Spring, no JPA in domain layer.
6. **Factory Methods Over Constructors**: For complex object creation.
7. **Events Are Published After Commit**: Use `@TransactionalEventListener`.
8. **Typed IDs Prevent Mixing**: `ProductId` can't be passed where `OrderId` is expected.
9. **Application Services Orchestrate**: Domain objects contain business logic.
10. **DataInitializer**: Must use application services or repositories.