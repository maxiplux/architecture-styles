package app.quantun.architecture.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilter {
    @Parameter(
            description = "Filter products by category ID. Only products belonging to this category will be returned.",
            example = "1"
    )
    private Long categoryId;

    @Parameter(
            description = "Filter products by name (case-insensitive partial match). Searches for products containing this text in their name.",
            example = "wireless"
    )
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Parameter(
            description = "Minimum price filter. Only products with price greater than or equal to this value will be returned.",
            example = "10.00"
    )
    @PositiveOrZero(message = "Minimum price must be positive or zero")
    private BigDecimal minPrice;

    @Parameter(
            description = "Maximum price filter. Only products with price less than or equal to this value will be returned.",
            example = "500.00"
    )
    @PositiveOrZero(message = "Maximum price must be positive or zero")
    private BigDecimal maxPrice;

    @Parameter(
            description = "Filter by stock availability. Set to true to show only in-stock products, false for out-of-stock products.",
            example = "true"
    )
    private Boolean inStock;

    @Parameter(
            description = "Filter by active status. Set to true to show only active products (default), false to show inactive products.",
            example = "true"
    )
    private Boolean active = true;

    @AssertTrue(message = "minPrice cannot be greater than maxPrice")
    public boolean isPriceRangeValid() {
        if (minPrice != null && maxPrice != null) {
            return minPrice.compareTo(maxPrice) <= 0;
        }
        return true;
    }
}
