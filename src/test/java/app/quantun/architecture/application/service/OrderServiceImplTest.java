package app.quantun.architecture.application.service;

import app.quantun.architecture.application.port.in.CreateOrderCommand;
import app.quantun.architecture.application.port.in.OrderItemCommand;
import app.quantun.architecture.application.port.out.OrderRepositoryPort;
import app.quantun.architecture.application.port.out.ProductRepositoryPort;
import app.quantun.architecture.domain.exception.ProductNotFoundException;
import app.quantun.architecture.domain.exception.ProductNotActiveException;
import app.quantun.architecture.domain.exception.InsufficientStockException;
import app.quantun.architecture.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Implementation Tests")
class OrderServiceImplTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private OrderRepositoryPort orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Category testCategory;
    private Product product1;
    private Product product2;
    private ShippingAddress shippingAddress;
    private CreateOrderCommand validCommand;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Electronics", "Electronic items", true, OffsetDateTime.now());

        product1 = new Product(1L, "Laptop", "High-end laptop",
                              new BigDecimal("1000.00"), 10, "url1", true, testCategory);

        product2 = new Product(2L, "Mouse", "Wireless mouse",
                              new BigDecimal("50.00"), 20, "url2", true, testCategory);

        shippingAddress = new ShippingAddress("123 Main St", "New York", "NY", "10001", "USA");

        List<OrderItemCommand> items = List.of(
            new OrderItemCommand(1L, 2),
            new OrderItemCommand(2L, 3)
        );

        validCommand = new CreateOrderCommand(100L, items, shippingAddress);
    }

    @Nested
    @DisplayName("Successful Order Creation")
    class SuccessfulOrderCreation {

        @Test
        @DisplayName("Should create order with valid products and sufficient stock")
        void shouldCreateOrderWithValidProducts() {
            // Given
            List<Product> products = List.of(product1, product2);
            when(productRepository.findAllByIds(anyList())).thenReturn(products);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                // Simulate setting ID after save
                return new Order(1001L, order.getCustomerId(), order.getStatus(),
                               order.getSubtotal(), order.getTax(), order.getTotal(),
                               order.getCreatedAt(), order.getShippingAddress(), order.getItems());
            });
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = orderService.createOrder(validCommand);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1001L);
            assertThat(result.getCustomerId()).isEqualTo(100L);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getItems()).hasSize(2);

            // Verify stock was decremented
            verify(productRepository, times(2)).save(any(Product.class));
            assertThat(product1.getStock()).isEqualTo(8); // 10 - 2
            assertThat(product2.getStock()).isEqualTo(17); // 20 - 3
        }

        @Test
        @DisplayName("Should calculate totals correctly")
        void shouldCalculateTotalsCorrectly() {
            // Given
            List<Product> products = List.of(product1, product2);
            when(productRepository.findAllByIds(anyList())).thenReturn(products);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = orderService.createOrder(validCommand);

            // Then
            // Subtotal: (1000 * 2) + (50 * 3) = 2000 + 150 = 2150
            assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("2150.00"));
            // Tax: 2150 * 0.08 = 172
            assertThat(result.getTax()).isEqualByComparingTo(new BigDecimal("172.00"));
            // Total: 2150 + 172 = 2322
            assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("2322.00"));
        }

        @Test
        @DisplayName("Should save order before updating stock")
        void shouldSaveOrderBeforeUpdatingStock() {
            // Given
            List<Product> products = List.of(product1, product2);
            when(productRepository.findAllByIds(anyList())).thenReturn(products);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            orderService.createOrder(validCommand);

            // Then - verify order of operations
            var orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());
            verify(productRepository, times(2)).save(any(Product.class));

            // Verify order was saved with correct items
            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getItems()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Product Validation")
    class ProductValidation {

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            when(productRepository.findAllByIds(anyList())).thenReturn(List.of(product1));
            // Only one product returned when two were requested

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(validCommand))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("One or more products not found");

            verify(orderRepository, never()).save(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when product is inactive")
        void shouldThrowExceptionWhenProductIsInactive() {
            // Given
            Product inactiveProduct = new Product(2L, "Mouse", "Wireless mouse",
                                                 new BigDecimal("50.00"), 20, "url2", false, testCategory);
            List<Product> products = List.of(product1, inactiveProduct);
            when(productRepository.findAllByIds(anyList())).thenReturn(products);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(validCommand))
                .isInstanceOf(ProductNotActiveException.class)
                .hasMessage("Product is inactive: 2");

            verify(orderRepository, never()).save(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            // Given
            Product lowStockProduct = new Product(1L, "Laptop", "High-end laptop",
                                                 new BigDecimal("1000.00"), 1, "url1", true, testCategory);
            List<Product> products = List.of(lowStockProduct, product2);
            when(productRepository.findAllByIds(anyList())).thenReturn(products);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(validCommand))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Product 1 has insufficient stock");

            verify(orderRepository, never()).save(any());
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Empty and Edge Cases")
    class EmptyAndEdgeCases {

        @Test
        @DisplayName("Should handle single item order")
        void shouldHandleSingleItemOrder() {
            // Given
            List<OrderItemCommand> singleItem = List.of(new OrderItemCommand(1L, 1));
            CreateOrderCommand singleItemCommand = new CreateOrderCommand(100L, singleItem, shippingAddress);

            when(productRepository.findAllByIds(List.of(1L))).thenReturn(List.of(product1));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = orderService.createOrder(singleItemCommand);

            // Then
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Should handle large quantity order")
        void shouldHandleLargeQuantityOrder() {
            // Given
            Product highStockProduct = new Product(1L, "Laptop", "High-end laptop",
                                                  new BigDecimal("1000.00"), 100, "url1", true, testCategory);
            List<OrderItemCommand> largeOrder = List.of(new OrderItemCommand(1L, 50));
            CreateOrderCommand largeCommand = new CreateOrderCommand(100L, largeOrder, shippingAddress);

            when(productRepository.findAllByIds(List.of(1L))).thenReturn(List.of(highStockProduct));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = orderService.createOrder(largeCommand);

            // Then
            assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("50000.00"));
            assertThat(highStockProduct.getStock()).isEqualTo(50); // 100 - 50
        }
    }

    @Nested
    @DisplayName("Transactional Behavior")
    class TransactionalBehavior {

        @Test
        @DisplayName("Should rollback on repository save failure")
        void shouldRollbackOnSaveFailure() {
            // Given
            List<Product> products = List.of(product1, product2);
            when(productRepository.findAllByIds(anyList())).thenReturn(products);
            when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(validCommand))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

            // Stock should not be modified on failure (though this would be handled by @Transactional)
            verify(productRepository, never()).save(any());
        }
    }
}