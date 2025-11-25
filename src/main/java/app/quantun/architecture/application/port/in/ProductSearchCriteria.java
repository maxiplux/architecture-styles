package app.quantun.architecture.application.port.in;

import java.math.BigDecimal;

public record ProductSearchCriteria(
    Long categoryId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean inStock,
    Boolean active
) {
    public ProductSearchCriteria {
        // Default active to true if not specified
        if (active == null) {
            active = true;
        }
    }
}