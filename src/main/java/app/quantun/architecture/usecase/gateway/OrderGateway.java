package app.quantun.architecture.usecase.gateway;

import app.quantun.architecture.entity.Order;

import java.util.Optional;

/**
 * Gateway interface for Order data access operations.
 * Defined in the use case layer, implemented in the interface adapter layer.
 */
public interface OrderGateway {
    Order save(Order order);
    Optional<Order> findById(Long id);
}
