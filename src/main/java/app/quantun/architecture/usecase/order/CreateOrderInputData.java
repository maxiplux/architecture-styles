package app.quantun.architecture.usecase.order;

import java.util.List;

/**
 * Input data for creating an order.
 */
public record CreateOrderInputData(
        Long customerId,
        List<OrderItemInputData> items,
        String shippingStreet,
        String shippingCity,
        String shippingState,
        String shippingZipCode,
        String shippingCountry
) {}
