package app.quantun.architecture.usecase.order;

import app.quantun.architecture.entity.Category;
import app.quantun.architecture.entity.Order;
import app.quantun.architecture.entity.Product;
import app.quantun.architecture.shared.exception.BusinessRuleException;
import app.quantun.architecture.shared.exception.EntityNotFoundException;
import app.quantun.architecture.shared.exception.ValidationException;
import app.quantun.architecture.usecase.gateway.OrderGateway;
import app.quantun.architecture.usecase.gateway.ProductGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderInteractorTest {

    @Mock
    private ProductGateway productGateway;

    @Mock
    private OrderGateway orderGateway;

    private CreateOrderInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new CreateOrderInteractor(productGateway, orderGateway);
    }

    @Test
    void execute_withValidInput_createsOrder() {
        Category category = Category.create(1L, "Electronics", "desc", true, OffsetDateTime.now());
        Product product = Product.create(1L, "Laptop", "Gaming laptop",
                new BigDecimal("1000.00"), 10, "img.jpg", true, category);

        CreateOrderInputData input = new CreateOrderInputData(
                1L,
                List.of(new OrderItemInputData(1L, 2)),
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        when(productGateway.findAllByIds(anyList())).thenReturn(List.of(product));
        when(orderGateway.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.assignId(100L);
            return order;
        });
        when(productGateway.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderOutputData result = interactor.execute(input);

        assertNotNull(result);
        assertEquals(100L, result.orderId());
        assertEquals("PENDING", result.status());
        assertEquals(1, result.items().size());
        verify(productGateway).findAllByIds(List.of(1L));
        verify(orderGateway).save(any(Order.class));
        verify(productGateway).save(any(Product.class));
    }

    @Test
    void execute_withEmptyItems_throwsValidationException() {
        CreateOrderInputData input = new CreateOrderInputData(
                1L,
                List.of(),
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        assertThrows(ValidationException.class, () -> interactor.execute(input));
        verify(productGateway, never()).findAllByIds(anyList());
        verify(orderGateway, never()).save(any());
    }

    @Test
    void execute_withNullItems_throwsValidationException() {
        CreateOrderInputData input = new CreateOrderInputData(
                1L,
                null,
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        assertThrows(ValidationException.class, () -> interactor.execute(input));
    }

    @Test
    void execute_withNonExistentProduct_throwsEntityNotFoundException() {
        CreateOrderInputData input = new CreateOrderInputData(
                1L,
                List.of(new OrderItemInputData(999L, 1)),
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        when(productGateway.findAllByIds(anyList())).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> interactor.execute(input));
    }

    @Test
    void execute_withInactiveProduct_throwsBusinessRuleException() {
        Category category = Category.create(1L, "Electronics", "desc", true, OffsetDateTime.now());
        Product inactiveProduct = Product.create(1L, "Laptop", "Gaming laptop",
                new BigDecimal("1000.00"), 10, "img.jpg", false, category);

        CreateOrderInputData input = new CreateOrderInputData(
                1L,
                List.of(new OrderItemInputData(1L, 2)),
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        when(productGateway.findAllByIds(anyList())).thenReturn(List.of(inactiveProduct));

        assertThrows(BusinessRuleException.class, () -> interactor.execute(input));
    }

    @Test
    void execute_withInsufficientStock_throwsBusinessRuleException() {
        Category category = Category.create(1L, "Electronics", "desc", true, OffsetDateTime.now());
        Product product = Product.create(1L, "Laptop", "Gaming laptop",
                new BigDecimal("1000.00"), 1, "img.jpg", true, category); // Only 1 in stock

        CreateOrderInputData input = new CreateOrderInputData(
                1L,
                List.of(new OrderItemInputData(1L, 5)), // Requesting 5
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        when(productGateway.findAllByIds(anyList())).thenReturn(List.of(product));

        assertThrows(BusinessRuleException.class, () -> interactor.execute(input));
        verify(orderGateway, never()).save(any());
    }
}
