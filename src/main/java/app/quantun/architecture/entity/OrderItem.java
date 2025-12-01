package app.quantun.architecture.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain entity representing an item within an order.
 * Pure domain object with no framework dependencies.
 */
public class OrderItem {
    private Long id;
    private final Long productId;
    private final String productName;
    private final BigDecimal unitPrice;
    private final Integer quantity;
    private final BigDecimal subtotal;

    private OrderItem(Long id, Long productId, String productName, BigDecimal unitPrice,
                      Integer quantity, BigDecimal subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    /**
     * Factory method to create a new OrderItem with calculated subtotal.
     */
    public static OrderItem create(Long productId, String productName, BigDecimal unitPrice, Integer quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        return new OrderItem(null, productId, productName, unitPrice, quantity, subtotal);
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static OrderItem reconstitute(Long id, Long productId, String productName,
                                          BigDecimal unitPrice, Integer quantity, BigDecimal subtotal) {
        return new OrderItem(id, productId, productName, unitPrice, quantity, subtotal);
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("OrderItem already has an ID");
        }
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
