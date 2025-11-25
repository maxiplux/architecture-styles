package app.quantun.architecture.application.port.in;

import app.quantun.architecture.domain.model.Order;

public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}