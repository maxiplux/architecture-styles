package app.quantun.architecture.adapter.out.persistence.adapter;

import app.quantun.architecture.adapter.out.persistence.mapper.OrderPersistenceMapper;
import app.quantun.architecture.adapter.out.persistence.repository.OrderJpaRepository;
import app.quantun.architecture.application.port.out.OrderRepositoryPort;
import app.quantun.architecture.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        var entity = mapper.toEntity(order);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}