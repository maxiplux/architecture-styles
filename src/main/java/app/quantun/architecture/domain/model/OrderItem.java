package app.quantun.architecture.domain.model;

import app.quantun.architecture.domain.exception.ProductNotActiveException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderItem {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public OrderItem(Long id, Long productId, String productName, Integer quantity,
                     BigDecimal unitPrice, BigDecimal subtotal) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.subtotal = subtotal != null ? subtotal.setScale(2, RoundingMode.HALF_UP) :
                       calculateSubtotal();
    }

    public static OrderItem create(Product product, Integer quantity) {
        if (!product.isActive()) {
            throw new ProductNotActiveException("Product is inactive: " + product.getId());
        }
        if (!product.hasEnoughStock(quantity)) {
            throw new app.quantun.architecture.domain.exception.InsufficientStockException(
                "Product " + product.getId() + " has insufficient stock. Available: " +
                product.getStock() + ", Requested: " + quantity
            );
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                                      .setScale(2, RoundingMode.HALF_UP);

        return new OrderItem(null, product.getId(), product.getName(), quantity, unitPrice, subtotal);
    }

    private BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                       .setScale(2, RoundingMode.HALF_UP);
    }

    // Getters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }

    // Package-private setter for persistence adapter to set ID after save
    void setId(Long id) {
        this.id = id;
    }
}