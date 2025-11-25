package app.quantun.architecture.adapter.out.persistence.mapper;

import app.quantun.architecture.adapter.out.persistence.entity.OrderJpaEntity;
import app.quantun.architecture.domain.model.Order;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {OrderItemPersistenceMapper.class})
public interface OrderPersistenceMapper {

    @Mapping(source = "shippingStreet", target = "shippingAddress.street")
    @Mapping(source = "shippingCity", target = "shippingAddress.city")
    @Mapping(source = "shippingState", target = "shippingAddress.state")
    @Mapping(source = "shippingZipCode", target = "shippingAddress.zipCode")
    @Mapping(source = "shippingCountry", target = "shippingAddress.country")
    Order toDomain(OrderJpaEntity entity);

    @Mapping(source = "shippingAddress.street", target = "shippingStreet")
    @Mapping(source = "shippingAddress.city", target = "shippingCity")
    @Mapping(source = "shippingAddress.state", target = "shippingState")
    @Mapping(source = "shippingAddress.zipCode", target = "shippingZipCode")
    @Mapping(source = "shippingAddress.country", target = "shippingCountry")
    OrderJpaEntity toEntity(Order order);

    @AfterMapping
    default void setOrderReference(@MappingTarget OrderJpaEntity entity) {
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> item.setOrder(entity));
        }
    }
}