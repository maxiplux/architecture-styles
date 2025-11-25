package app.quantun.architecture.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public Order(Long id, Long customerId, OrderStatus status, BigDecimal subtotal,
                 BigDecimal tax, BigDecimal total, OffsetDateTime createdAt,
                 ShippingAddress shippingAddress, List<OrderItem> items) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address cannot be null");
        }
        this.id = id;
        this.customerId = customerId;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.shippingAddress = shippingAddress;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();

        if (subtotal != null && tax != null && total != null) {
            this.subtotal = subtotal;
            this.tax = tax;
            this.total = total;
        } else {
            calculateTotals();
        }
    }

    // Factory method for creating new orders
    public static Order create(Long customerId, List<OrderItem> items, ShippingAddress shippingAddress) {
        return new Order(null, customerId, OrderStatus.PENDING, null, null, null,
                        OffsetDateTime.now(), shippingAddress, items);
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
    public Long getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }

    // Package-private setter for persistence adapter to set ID after save
    void setId(Long id) {
        this.id = id;
    }
}