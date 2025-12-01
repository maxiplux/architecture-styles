package app.quantun.architecture.usecase.order;

import app.quantun.architecture.entity.Order;
import app.quantun.architecture.entity.OrderItem;
import app.quantun.architecture.entity.Product;
import app.quantun.architecture.entity.ShippingAddress;
import app.quantun.architecture.shared.exception.BusinessRuleException;
import app.quantun.architecture.shared.exception.EntityNotFoundException;
import app.quantun.architecture.shared.exception.ValidationException;
import app.quantun.architecture.usecase.gateway.OrderGateway;
import app.quantun.architecture.usecase.gateway.ProductGateway;

import java.util.ArrayList;
import java.util.List;

/**
 * Interactor implementing the CreateOrder use case.
 * Pure Java class with no framework annotations.
 * Transaction management is handled at the interface adapter level.
 */
public class CreateOrderInteractor implements CreateOrderUseCase {

    private final ProductGateway productGateway;
    private final OrderGateway orderGateway;

    public CreateOrderInteractor(ProductGateway productGateway, OrderGateway orderGateway) {
        this.productGateway = productGateway;
        this.orderGateway = orderGateway;
    }

    @Override
    public OrderOutputData execute(CreateOrderInputData input) {
        // Validate input
        if (input.items() == null || input.items().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }

        // Load products
        List<Long> productIds = input.items().stream()
                .map(OrderItemInputData::productId)
                .toList();

        List<Product> products = productGateway.findAllByIds(productIds);

        if (products.size() != productIds.size()) {
            throw new EntityNotFoundException("One or more products not found");
        }

        // Build order items using entity business logic
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemInputData itemInput : input.items()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(itemInput.productId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemInput.productId()));

            // Validate product is active
            if (!product.isActive()) {
                throw new BusinessRuleException("Product is inactive: " + product.getId());
            }

            // Entity validates its own rules
            if (!product.canFulfillQuantity(itemInput.quantity())) {
                throw new BusinessRuleException(
                        "Insufficient stock for product: " + product.getId()
                );
            }

            OrderItem orderItem = OrderItem.create(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    itemInput.quantity()
            );
            orderItems.add(orderItem);
        }

        // Create shipping address (value object validates itself)
        ShippingAddress shippingAddress = new ShippingAddress(
                input.shippingStreet(),
                input.shippingCity(),
                input.shippingState(),
                input.shippingZipCode(),
                input.shippingCountry()
        );

        // Create order (entity validates and calculates totals)
        Order order = Order.createNew(input.customerId(), orderItems, shippingAddress);

        // Persist order
        Order savedOrder = orderGateway.save(order);

        // Update product stock (entity business logic)
        for (OrderItem item : savedOrder.getItems()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst()
                    .orElseThrow();

            product.reduceStock(item.getQuantity());
            productGateway.save(product);
        }

        return toOutputData(savedOrder);
    }

    private OrderOutputData toOutputData(Order order) {
        List<OrderItemOutputData> items = order.getItems().stream()
                .map(item -> new OrderItemOutputData(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderOutputData(
                order.getId(),
                order.getStatus().name(),
                items,
                order.getSubtotal(),
                order.getTax(),
                order.getTotal(),
                order.getCreatedAt()
        );
    }
}
