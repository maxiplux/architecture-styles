package app.quantun.architecture.domain.model;

import app.quantun.architecture.domain.exception.InsufficientStockException;
import java.math.BigDecimal;

public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private boolean active;
    private Category category;

    public Product(Long id, String name, String description, BigDecimal price,
                   Integer stock, String imageUrl, boolean active, Category category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be non-negative");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Product stock must be non-negative");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.active = active;
        this.category = category;
    }

    public static Product create(String name, String description, BigDecimal price,
                                Integer stock, String imageUrl, Category category) {
        return new Product(null, name, description, price, stock, imageUrl, true, category);
    }

    // Business methods
    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    public boolean hasEnoughStock(int quantity) {
        return stock != null && stock >= quantity;
    }

    public void decrementStock(int quantity) {
        if (!hasEnoughStock(quantity)) {
            throw new InsufficientStockException(
                "Product " + id + " has insufficient stock. Available: " + stock + ", Requested: " + quantity
            );
        }
        this.stock -= quantity;
    }

    // Getters (no setters - encourage immutability or controlled mutation)
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public Category getCategory() { return category; }

    // Package-private setter for persistence adapter to set ID after save
    void setId(Long id) {
        this.id = id;
    }
}