package app.quantun.architecture.adapter.out.persistence.specification;

import app.quantun.architecture.adapter.out.persistence.entity.ProductJpaEntity;
import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductJpaSpecifications {

    private ProductJpaSpecifications() {}

    public static Specification<ProductJpaEntity> fromCriteria(ProductSearchCriteria criteria) {
        return Specification.allOf(
            hasCategory(criteria.categoryId()),
            nameLike(criteria.name()),
            priceBetween(criteria.minPrice(), criteria.maxPrice()),
            inStock(criteria.inStock()),
            isActive(criteria.active())
        );
    }

    private static Specification<ProductJpaEntity> hasCategory(Long categoryId) {
        return (root, query, cb) ->
            categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<ProductJpaEntity> nameLike(String name) {
        return (root, query, cb) ->
            (name == null || name.isBlank()) ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<ProductJpaEntity> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }

    private static Specification<ProductJpaEntity> inStock(Boolean inStock) {
        return (root, query, cb) ->
            (inStock == null || !inStock) ? null : cb.greaterThan(root.get("stock"), 0);
    }

    private static Specification<ProductJpaEntity> isActive(Boolean active) {
        return (root, query, cb) ->
            active == null ? null : cb.equal(root.get("active"), active);
    }
}