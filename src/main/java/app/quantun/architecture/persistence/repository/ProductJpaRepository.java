package app.quantun.architecture.persistence.repository;

import app.quantun.architecture.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data JPA repository for Product persistence.
 * Extends JpaSpecificationExecutor for dynamic query support.
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long>, JpaSpecificationExecutor<ProductJpaEntity> {
}
