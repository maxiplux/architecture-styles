package app.quantun.architecture.dto;

import java.math.BigDecimal;

public record ProductDTO(
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
