package app.quantun.architecture.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain entity representing a customer order.
 * Pure domain object with business logic, no framework dependencies.
 */
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

    /**
     * Factory method for creating new orders with automatic calculation of totals.
     */
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
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static Order reconstitute(Long id, Long customerId, OrderStatus status,
                                      List<OrderItem> items, BigDecimal subtotal,
                                      BigDecimal tax, BigDecimal total,
                                      OffsetDateTime createdAt, ShippingAddress shippingAddress) {
        return new Order(id, customerId, status, new ArrayList<>(items), subtotal, tax, total, createdAt, shippingAddress);
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

    /**
     * Confirms a pending order.
     */
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * Cancels the order if it hasn't been shipped yet.
     */
    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel shipped or delivered orders");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Assigns an ID to a newly created order (after persistence).
     */
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Order already has an ID");
        }
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }
}
