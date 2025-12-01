package app.quantun.architecture.presentation.rest.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response model for order data in HTTP responses.
 */
public record OrderResponse(
        Long orderId,
        String status,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        OffsetDateTime createdAt
) {}
