package app.quantun.architecture.adapter.in.web.mapper;

import app.quantun.architecture.adapter.in.web.dto.*;
import app.quantun.architecture.application.port.in.CreateOrderCommand;
import app.quantun.architecture.application.port.in.OrderItemCommand;
import app.quantun.architecture.domain.model.Order;
import app.quantun.architecture.domain.model.OrderItem;
import app.quantun.architecture.domain.model.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderWebMapper {

    CreateOrderCommand toCommand(OrderCreateRequest request);

    @Mapping(source = "id", target = "orderId")
    OrderResponse toResponse(Order order);

    OrderItemCommand toItemCommand(OrderItemRequest itemRequest);

    OrderItemResponse toItemResponse(OrderItem item);

    ShippingAddress toShippingAddress(ShippingAddressRequest request);
}