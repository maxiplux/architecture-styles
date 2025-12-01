package app.quantun.architecture.persistence.repository;

import app.quantun.architecture.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for Order persistence.
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
}
