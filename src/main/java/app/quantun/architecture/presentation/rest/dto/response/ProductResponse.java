package app.quantun.architecture.presentation.rest.dto.response;

import java.math.BigDecimal;

/**
 * Response model for product data in HTTP responses.
 */
public record ProductResponse(
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
