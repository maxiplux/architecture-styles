package app.quantun.architecture.adapter.out.persistence.repository;

import app.quantun.architecture.adapter.out.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductJpaRepository extends
    JpaRepository<ProductJpaEntity, Long>,
    JpaSpecificationExecutor<ProductJpaEntity> {
}