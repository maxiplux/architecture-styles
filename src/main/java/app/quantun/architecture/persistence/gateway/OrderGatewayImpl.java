package app.quantun.architecture.persistence.gateway;

import app.quantun.architecture.entity.Order;
import app.quantun.architecture.persistence.entity.OrderJpaEntity;
import app.quantun.architecture.persistence.mapper.OrderPersistenceMapper;
import app.quantun.architecture.persistence.repository.OrderJpaRepository;
import app.quantun.architecture.usecase.gateway.OrderGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Gateway implementation for Order data access.
 * Bridges use case layer with persistence layer.
 * Transaction boundaries are defined here.
 */
@Component
public class OrderGatewayImpl implements OrderGateway {

    private final OrderJpaRepository repository;
    private final OrderPersistenceMapper mapper;

    public OrderGatewayImpl(OrderJpaRepository repository, OrderPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);
        OrderJpaEntity saved = repository.save(entity);

        // Assign generated ID back to domain entity
        if (order.getId() == null) {
            order.assignId(saved.getId());
        }

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
