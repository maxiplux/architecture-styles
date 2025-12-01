package app.quantun.architecture.entity;

import java.math.BigDecimal;

/**
 * Domain entity representing a product.
 * Pure domain object with business logic, no framework dependencies.
 */
public class Product {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private Integer stock;
    private final String imageUrl;
    private final boolean active;
    private final Category category;

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

    /**
     * Factory method to create a Product with validation.
     */
    public static Product create(Long id, String name, String description, BigDecimal price,
                                  Integer stock, String imageUrl, boolean active, Category category) {
        if (name == null || name.isBlank()) {
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

    /**
     * Factory method for reconstituting from persistence.
     */
    public static Product reconstitute(Long id, String name, String description, BigDecimal price,
                                        Integer stock, String imageUrl, boolean active, Category category) {
        return new Product(id, name, description, price, stock, imageUrl, active, category);
    }

    /**
     * Checks if the product is available for purchase.
     */
    public boolean isAvailable() {
        return active && stock > 0;
    }

    /**
     * Checks if the product can fulfill a specific quantity.
     */
    public boolean canFulfillQuantity(int quantity) {
        return active && stock >= quantity;
    }

    /**
     * Reduces stock by the specified quantity.
     * @throws IllegalStateException if stock is insufficient or product is inactive
     */
    public void reduceStock(int quantity) {
        if (!canFulfillQuantity(quantity)) {
            throw new IllegalStateException(
                    String.format("Cannot reduce stock by %d. Current stock: %d, Active: %s",
                            quantity, stock, active)
            );
        }
        this.stock -= quantity;
    }

    /**
     * Creates a copy of this product with updated stock.
     */
    public Product withStock(Integer newStock) {
        return new Product(id, name, description, price, newStock, imageUrl, active, category);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public Category getCategory() {
        return category;
    }
}
