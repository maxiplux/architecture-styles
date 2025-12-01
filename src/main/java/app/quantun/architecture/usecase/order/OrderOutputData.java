package app.quantun.architecture.usecase.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Output data for an order.
 */
public record OrderOutputData(
        Long orderId,
        String status,
        List<OrderItemOutputData> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        OffsetDateTime createdAt
) {}
