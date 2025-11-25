package app.quantun.architecture.application.port.out;

import app.quantun.architecture.domain.model.Order;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
}