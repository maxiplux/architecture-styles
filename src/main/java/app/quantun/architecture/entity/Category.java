package app.quantun.architecture.entity;

import java.time.OffsetDateTime;

/**
 * Domain entity representing a product category.
 * Pure domain object with no framework dependencies.
 */
public class Category {
    private final Long id;
    private final String name;
    private final String description;
    private final boolean active;
    private final OffsetDateTime createdAt;

    private Category(Long id, String name, String description, boolean active, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    /**
     * Factory method to create a new Category with validation.
     */
    public static Category create(Long id, String name, String description, boolean active, OffsetDateTime createdAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Category name must not exceed 100 characters");
        }
        return new Category(id, name, description, active, createdAt != null ? createdAt : OffsetDateTime.now());
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static Category reconstitute(Long id, String name, String description, boolean active, OffsetDateTime createdAt) {
        return new Category(id, name, description, active, createdAt);
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

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
