package app.quantun.architecture.adapter.out.persistence.mapper;

import app.quantun.architecture.adapter.out.persistence.entity.OrderItemJpaEntity;
import app.quantun.architecture.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemPersistenceMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItem toDomain(OrderItemJpaEntity entity);

    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "productName", target = "product.name")
    OrderItemJpaEntity toEntity(OrderItem orderItem);
}