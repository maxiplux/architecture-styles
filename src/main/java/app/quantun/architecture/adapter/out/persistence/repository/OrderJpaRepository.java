package app.quantun.architecture.adapter.out.persistence.repository;

import app.quantun.architecture.adapter.out.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
}