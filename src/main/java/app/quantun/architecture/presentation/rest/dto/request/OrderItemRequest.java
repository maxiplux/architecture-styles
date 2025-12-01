package app.quantun.architecture.presentation.rest.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request model for order item data in HTTP requests.
 */
public record OrderItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) @Max(99) Integer quantity
) {}
