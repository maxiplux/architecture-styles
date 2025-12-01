package app.quantun.architecture.persistence.specification;

import app.quantun.architecture.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * JPA Specifications for dynamic product queries.
 * Part of the persistence layer.
 */
public final class ProductSpecifications {
    private ProductSpecifications() {}

    public static Specification<ProductJpaEntity> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<ProductJpaEntity> nameLike(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank()) ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<ProductJpaEntity> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    public static Specification<ProductJpaEntity> inStock(Boolean inStock) {
        return (root, query, cb) -> (inStock == null || !inStock) ? null : cb.greaterThan(root.get("stock"), 0);
    }

    public static Specification<ProductJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> (active == null) ? null : cb.equal(root.get("active"), active);
    }
}
