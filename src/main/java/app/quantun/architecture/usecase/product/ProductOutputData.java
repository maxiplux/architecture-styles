package app.quantun.architecture.usecase.product;

import java.math.BigDecimal;

/**
 * Output data for a single product.
 */
public record ProductOutputData(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Long categoryId,
        String categoryName,
        Integer stock,
        String imageUrl,
        boolean active
) {}
