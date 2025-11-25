package app.quantun.architecture.application.port.out;

import app.quantun.architecture.domain.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {
    List<Category> findAllActive();
    Optional<Category> findById(Long id);
    boolean existsById(Long id);
    Category save(Category category);
}