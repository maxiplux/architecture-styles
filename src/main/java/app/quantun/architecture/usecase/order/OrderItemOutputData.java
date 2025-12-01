package app.quantun.architecture.usecase.order;

import java.math.BigDecimal;

/**
 * Output data for an order item.
 */
public record OrderItemOutputData(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
