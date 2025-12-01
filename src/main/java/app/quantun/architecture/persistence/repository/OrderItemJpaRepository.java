package app.quantun.architecture.persistence.repository;

import app.quantun.architecture.persistence.entity.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for OrderItem persistence.
 */
public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {
}
