package app.quantun.architecture.persistence.gateway;

import app.quantun.architecture.entity.Category;
import app.quantun.architecture.persistence.mapper.CategoryPersistenceMapper;
import app.quantun.architecture.persistence.repository.CategoryJpaRepository;
import app.quantun.architecture.usecase.gateway.CategoryGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Gateway implementation for Category data access.
 * Bridges use case layer with persistence layer.
 */
@Component
public class CategoryGatewayImpl implements CategoryGateway {

    private final CategoryJpaRepository repository;
    private final CategoryPersistenceMapper mapper;

    public CategoryGatewayImpl(CategoryJpaRepository repository, CategoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Category> findAllActive() {
        return repository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
