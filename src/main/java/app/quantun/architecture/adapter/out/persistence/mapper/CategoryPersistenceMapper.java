package app.quantun.architecture.adapter.out.persistence.mapper;

import app.quantun.architecture.adapter.out.persistence.entity.CategoryJpaEntity;
import app.quantun.architecture.domain.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryPersistenceMapper {

    Category toDomain(CategoryJpaEntity entity);

    CategoryJpaEntity toEntity(Category category);
}