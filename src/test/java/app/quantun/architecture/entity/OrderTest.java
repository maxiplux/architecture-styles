package app.quantun.architecture.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void createNew_withValidData_calculatesTotalsCorrectly() {
        OrderItem item1 = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 2);
        OrderItem item2 = OrderItem.create(2L, "Mouse", new BigDecimal("50.00"), 1);
        List<OrderItem> items = List.of(item1, item2);

        ShippingAddress address = new ShippingAddress(
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        Order order = Order.createNew(1L, items, address);

        assertEquals(new BigDecimal("2050.00"), order.getSubtotal());
        assertEquals(new BigDecimal("164.00"), order.getTax()); // 8% of 2050
        assertEquals(new BigDecimal("2214.00"), order.getTotal());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    void createNew_withEmptyItems_throwsException() {
        ShippingAddress address = new ShippingAddress(
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        assertThrows(IllegalArgumentException.class, () ->
                Order.createNew(1L, List.of(), address)
        );
    }

    @Test
    void createNew_withNullItems_throwsException() {
        ShippingAddress address = new ShippingAddress(
                "123 Main St", "Springfield", "IL", "62701", "USA"
        );

        assertThrows(IllegalArgumentException.class, () ->
                Order.createNew(1L, null, address)
        );
    }

    @Test
    void createNew_withNullShippingAddress_throwsException() {
        OrderItem item = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 1);

        assertThrows(IllegalArgumentException.class, () ->
                Order.createNew(1L, List.of(item), null)
        );
    }

    @Test
    void confirm_pendingOrder_changesStatusToConfirmed() {
        OrderItem item = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 1);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "ST", "12345", "USA");
        Order order = Order.createNew(1L, List.of(item), address);

        order.confirm();

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void confirm_nonPendingOrder_throwsException() {
        OrderItem item = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 1);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "ST", "12345", "USA");
        Order order = Order.createNew(1L, List.of(item), address);
        order.confirm();

        assertThrows(IllegalStateException.class, order::confirm);
    }

    @Test
    void cancel_pendingOrder_changesStatusToCancelled() {
        OrderItem item = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 1);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "ST", "12345", "USA");
        Order order = Order.createNew(1L, List.of(item), address);

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void assignId_toNewOrder_assignsSuccessfully() {
        OrderItem item = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 1);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "ST", "12345", "USA");
        Order order = Order.createNew(1L, List.of(item), address);

        order.assignId(100L);

        assertEquals(100L, order.getId());
    }

    @Test
    void assignId_toOrderWithExistingId_throwsException() {
        OrderItem item = OrderItem.create(1L, "Laptop", new BigDecimal("1000.00"), 1);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "ST", "12345", "USA");
        Order order = Order.createNew(1L, List.of(item), address);
        order.assignId(100L);

        assertThrows(IllegalStateException.class, () -> order.assignId(200L));
    }
}
