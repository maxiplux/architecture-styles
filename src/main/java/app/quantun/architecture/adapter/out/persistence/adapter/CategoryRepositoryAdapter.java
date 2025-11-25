package app.quantun.architecture.adapter.out.persistence.adapter;

import app.quantun.architecture.adapter.out.persistence.mapper.CategoryPersistenceMapper;
import app.quantun.architecture.adapter.out.persistence.repository.CategoryJpaRepository;
import app.quantun.architecture.application.port.out.CategoryRepositoryPort;
import app.quantun.architecture.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository jpaRepository;
    private final CategoryPersistenceMapper mapper;

    @Override
    public List<Category> findAllActive() {
        return jpaRepository.findAllActiveOrderByName().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Category save(Category category) {
        var entity = mapper.toEntity(category);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}