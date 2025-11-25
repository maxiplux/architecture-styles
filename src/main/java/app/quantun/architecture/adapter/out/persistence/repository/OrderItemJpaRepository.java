package app.quantun.architecture.adapter.out.persistence.repository;

import app.quantun.architecture.adapter.out.persistence.entity.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {
}