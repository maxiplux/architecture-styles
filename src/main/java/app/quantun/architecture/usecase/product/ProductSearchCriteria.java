package app.quantun.architecture.usecase.product;

import java.math.BigDecimal;

/**
 * Input data for product search operations.
 */
public record ProductSearchCriteria(
        Long categoryId,
        String name,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        Boolean active,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public ProductSearchCriteria {
        if (active == null) active = true;
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null) sortDirection = "asc";
    }

    public static ProductSearchCriteria of(Long categoryId, String name, BigDecimal minPrice,
                                           BigDecimal maxPrice, Boolean inStock, Boolean active,
                                           int page, int size, String sortBy, String sortDirection) {
        return new ProductSearchCriteria(categoryId, name, minPrice, maxPrice, inStock, active,
                page, size, sortBy, sortDirection);
    }
}
