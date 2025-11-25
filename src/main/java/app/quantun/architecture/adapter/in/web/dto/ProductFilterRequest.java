package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductFilterRequest(
    @Schema(description = "Filter by category ID", example = "1")
    Long categoryId,

    @Schema(description = "Filter by product name (partial match)", example = "phone")
    String name,

    @Schema(description = "Minimum price filter", example = "100.00")
    BigDecimal minPrice,

    @Schema(description = "Maximum price filter", example = "1000.00")
    BigDecimal maxPrice,

    @Schema(description = "Filter by stock availability", example = "true")
    Boolean inStock,

    @Schema(description = "Filter by active status", example = "true")
    Boolean active
) {}