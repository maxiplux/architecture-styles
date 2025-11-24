package app.quantun.architecture.spec;

import app.quantun.architecture.domain.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecifications {
    private ProductSpecifications() {}

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> nameLike(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank()) ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    public static Specification<Product> inStock(Boolean inStock) {
        return (root, query, cb) -> (inStock == null || !inStock) ? null : cb.greaterThan(root.get("stock"), 0);
    }

    public static Specification<Product> isActive(Boolean active) {
        return (root, query, cb) -> (active == null) ? null : cb.equal(root.get("active"), active);
    }
}
