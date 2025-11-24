package app.quantun.architecture.dto.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String status,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        OffsetDateTime createdAt
) {}
