package app.quantun.architecture.application.port.in;

import app.quantun.architecture.domain.model.ShippingAddress;
import java.util.List;

public record CreateOrderCommand(
    Long customerId,
    List<OrderItemCommand> items,
    ShippingAddress shippingAddress
) {}