package app.quantun.architecture.presentation.rest.dto.response;

import java.math.BigDecimal;

/**
 * Response model for order item data in HTTP responses.
 */
public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
