package app.quantun.architecture.adapter.out.persistence.repository;

import app.quantun.architecture.adapter.out.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.active = true ORDER BY c.name")
    List<CategoryJpaEntity> findAllActiveOrderByName();
}