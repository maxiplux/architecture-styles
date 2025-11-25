package app.quantun.architecture.mapper;

import app.quantun.architecture.domain.CustomerOrder;
import app.quantun.architecture.domain.OrderItem;
import app.quantun.architecture.dto.order.OrderItemResponse;
import app.quantun.architecture.dto.order.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponse toItemResponse(OrderItem orderItem);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "items", target = "items")
    OrderResponse toResponse(CustomerOrder order);
}
