package app.quantun.architecture.domain.model;

import java.time.OffsetDateTime;

public class Category {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private OffsetDateTime createdAt;

    public Category(Long id, String name, String description, boolean active, OffsetDateTime createdAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public static Category create(String name, String description) {
        return new Category(null, name, description, true, OffsetDateTime.now());
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

    // Package-private setter for persistence adapter to set ID after save
    void setId(Long id) {
        this.id = id;
    }
}