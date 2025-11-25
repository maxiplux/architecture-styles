package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductResponse(
    @Schema(description = "Unique identifier of the product", example = "101")
    Long id,

    @Schema(description = "Name of the product", example = "Smartphone")
    String name,

    @Schema(description = "Description of the product", example = "Latest model smartphone with 5G")
    String description,

    @Schema(description = "Price of the product", example = "999.99")
    BigDecimal price,

    @Schema(description = "ID of the category", example = "1")
    Long categoryId,

    @Schema(description = "Name of the category", example = "Electronics")
    String categoryName,

    @Schema(description = "Current stock quantity", example = "50")
    Integer stock,

    @Schema(description = "URL of the product image", example = "http://example.com/images/phone.jpg")
    String imageUrl,

    @Schema(description = "Whether the product is active", example = "true")
    boolean active
) {}