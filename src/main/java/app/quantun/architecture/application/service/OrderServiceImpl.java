package app.quantun.architecture.application.service;

import app.quantun.architecture.application.port.in.CreateOrderCommand;
import app.quantun.architecture.application.port.in.CreateOrderUseCase;
import app.quantun.architecture.application.port.in.OrderItemCommand;
import app.quantun.architecture.application.port.out.OrderRepositoryPort;
import app.quantun.architecture.application.port.out.ProductRepositoryPort;
import app.quantun.architecture.domain.exception.ProductNotFoundException;
import app.quantun.architecture.domain.model.Order;
import app.quantun.architecture.domain.model.OrderItem;
import app.quantun.architecture.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements CreateOrderUseCase {

    private final ProductRepositoryPort productRepository;
    private final OrderRepositoryPort orderRepository;

    @Override
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // Load all products
        List<Long> productIds = command.items().stream()
            .map(OrderItemCommand::productId)
            .toList();

        List<Product> products = productRepository.findAllByIds(productIds);

        // Validate all products exist
        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException("One or more products not found");
        }

        // Create order items and validate stock
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemCommand itemCmd : command.items()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(itemCmd.productId()))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemCmd.productId()));

            // Create order item (validates stock and active status in domain)
            OrderItem orderItem = OrderItem.create(product, itemCmd.quantity());
            orderItems.add(orderItem);
        }

        // Create order using domain factory method
        Order order = Order.create(command.customerId(), orderItems, command.shippingAddress());

        // Persist order
        Order savedOrder = orderRepository.save(order);

        // Decrement stock (domain logic)
        for (OrderItem item : savedOrder.getItems()) {
            Product product = products.stream()
                .filter(p -> p.getId().equals(item.getProductId()))
                .findFirst()
                .orElseThrow();

            product.decrementStock(item.getQuantity());
            productRepository.save(product);
        }

        return savedOrder;
    }
}