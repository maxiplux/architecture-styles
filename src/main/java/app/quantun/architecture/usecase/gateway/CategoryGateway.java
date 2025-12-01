package app.quantun.architecture.usecase.gateway;

import app.quantun.architecture.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Gateway interface for Category data access operations.
 * Defined in the use case layer, implemented in the interface adapter layer.
 */
public interface CategoryGateway {
    List<Category> findAllActive();
    Optional<Category> findById(Long id);
    boolean existsById(Long id);
}
