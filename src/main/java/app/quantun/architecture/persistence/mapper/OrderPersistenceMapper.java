package app.quantun.architecture.persistence.mapper;

import app.quantun.architecture.entity.Order;
import app.quantun.architecture.entity.OrderItem;
import app.quantun.architecture.entity.ShippingAddress;
import app.quantun.architecture.persistence.entity.OrderJpaEntity;
import app.quantun.architecture.persistence.entity.OrderItemJpaEntity;
import app.quantun.architecture.persistence.entity.ProductJpaEntity;
import app.quantun.architecture.persistence.repository.ProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for transforming between Order domain entity and OrderJpaEntity JPA entity.
 */
@Component
public class OrderPersistenceMapper {

    private final ProductJpaRepository productRepository;

    public OrderPersistenceMapper(ProductJpaRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<OrderItem> items = entity.getItems().stream()
                .map(this::itemToDomain)
                .toList();

        ShippingAddress shippingAddress = new ShippingAddress(
                entity.getShippingStreet(),
                entity.getShippingCity(),
                entity.getShippingState(),
                entity.getShippingZipCode(),
                entity.getShippingCountry()
        );

        return Order.reconstitute(
                entity.getId(),
                entity.getCustomerId(),
                entity.getStatus(),
                items,
                entity.getSubtotal(),
                entity.getTax(),
                entity.getTotal(),
                entity.getCreatedAt(),
                shippingAddress
        );
    }

    private OrderItem itemToDomain(OrderItemJpaEntity itemEntity) {
        return OrderItem.reconstitute(
                itemEntity.getId(),
                itemEntity.getProduct().getId(),
                itemEntity.getProduct().getName(),
                itemEntity.getUnitPrice(),
                itemEntity.getQuantity(),
                itemEntity.getSubtotal()
        );
    }

    public OrderJpaEntity toJpaEntity(Order order) {
        if (order == null) {
            return null;
        }

        OrderJpaEntity entity = OrderJpaEntity.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .shippingStreet(order.getShippingAddress().street())
                .shippingCity(order.getShippingAddress().city())
                .shippingState(order.getShippingAddress().state())
                .shippingZipCode(order.getShippingAddress().zipCode())
                .shippingCountry(order.getShippingAddress().country())
                .items(new ArrayList<>())
                .build();

        // Map order items and link to order
        for (OrderItem item : order.getItems()) {
            ProductJpaEntity productEntity = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + item.getProductId()));

            OrderItemJpaEntity itemEntity = OrderItemJpaEntity.builder()
                    .id(item.getId())
                    .order(entity)
                    .product(productEntity)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(item.getSubtotal())
                    .build();

            entity.getItems().add(itemEntity);
        }

        return entity;
    }
}
