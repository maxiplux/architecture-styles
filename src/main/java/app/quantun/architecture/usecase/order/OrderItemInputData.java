package app.quantun.architecture.usecase.order;

/**
 * Input data for an order item.
 */
public record OrderItemInputData(
        Long productId,
        Integer quantity
) {}
