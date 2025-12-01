package app.quantun.architecture.persistence.repository;

import app.quantun.architecture.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for Category persistence.
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
    List<CategoryJpaEntity> findByActiveTrue();
}
