package app.quantun.architecture.persistence.mapper;

import app.quantun.architecture.entity.Category;
import app.quantun.architecture.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for transforming between Category domain entity and CategoryJpaEntity JPA entity.
 */
@Component
public class CategoryPersistenceMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Category.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public CategoryJpaEntity toJpaEntity(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryJpaEntity.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
