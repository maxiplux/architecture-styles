package app.quantun.architecture.adapter.out.persistence.mapper;

import app.quantun.architecture.adapter.out.persistence.entity.ProductJpaEntity;
import app.quantun.architecture.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryPersistenceMapper.class})
public interface ProductPersistenceMapper {

    Product toDomain(ProductJpaEntity entity);

    ProductJpaEntity toEntity(Product product);
}