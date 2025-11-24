package app.quantun.architecture.service;

import app.quantun.architecture.domain.CustomerOrder;
import app.quantun.architecture.domain.OrderItem;
import app.quantun.architecture.domain.OrderStatus;
import app.quantun.architecture.domain.Product;
import app.quantun.architecture.dto.order.*;
import app.quantun.architecture.exception.BadRequestException;
import app.quantun.architecture.exception.ConflictException;
import app.quantun.architecture.repository.CustomerOrderRepository;
import app.quantun.architecture.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8%

    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order items cannot be empty");
        }

        // Load and validate products, stock
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new BadRequestException("Invalid product ID: " + itemReq.getProductId()));

            if (!Boolean.TRUE.equals(product.isActive())) {
                throw new ConflictException("Product is inactive: " + product.getId());
            }
            if (product.getStock() < itemReq.getQuantity()) {
                throw new ConflictException("Insufficient stock for product: " + product.getId());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(lineSubtotal);

            OrderItem oi = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(lineSubtotal)
                    .build();
            orderItems.add(oi);
        }

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        // Create order
        ShippingAddressDTO addr = request.getShippingAddress();
        CustomerOrder order = CustomerOrder.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .tax(tax)
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .shippingStreet(addr.getStreet())
                .shippingCity(addr.getCity())
                .shippingState(addr.getState())
                .shippingZipCode(addr.getZipCode())
                .shippingCountry(addr.getCountry())
                .build();

        // Link items to order
        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
            order.getItems().add(oi);
        }

        // Persist order (cascade saves items)
        CustomerOrder saved = orderRepository.save(order);

        // Decrement stock
        for (OrderItem oi : saved.getItems()) {
            Product p = oi.getProduct();
            p.setStock(p.getStock() - oi.getQuantity());
            productRepository.save(p);
        }

        // Build response
        List<OrderItemResponse> itemResponses = saved.getItems().stream()
                .map(oi -> new OrderItemResponse(
                        oi.getProduct().getId(),
                        oi.getProduct().getName(),
                        oi.getQuantity(),
                        oi.getUnitPrice(),
                        oi.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                saved.getId(),
                saved.getStatus().name(),
                itemResponses,
                saved.getSubtotal(),
                saved.getTax(),
                saved.getTotal(),
                saved.getCreatedAt()
        );
    }
}
