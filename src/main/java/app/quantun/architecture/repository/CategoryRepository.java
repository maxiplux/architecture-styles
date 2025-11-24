package app.quantun.architecture.repository;

import app.quantun.architecture.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    java.util.List<Category> findByActiveTrue();
}
