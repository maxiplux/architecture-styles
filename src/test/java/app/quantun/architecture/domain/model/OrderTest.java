package app.quantun.architecture.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Domain Model Tests")
class OrderTest {

    private ShippingAddress testAddress;
    private List<OrderItem> testItems;
    private Category testCategory;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testAddress = new ShippingAddress("123 Main St", "New York", "NY", "10001", "USA");
        testCategory = new Category(1L, "Electronics", "Electronic items", true, OffsetDateTime.now());

        testProduct1 = new Product(1L, "Laptop", "High-end laptop",
                                   new BigDecimal("1000.00"), 10, "url1", true, testCategory);
        testProduct2 = new Product(2L, "Mouse", "Wireless mouse",
                                   new BigDecimal("50.00"), 20, "url2", true, testCategory);

        testItems = new ArrayList<>();
        testItems.add(new OrderItem(null, 1L, "Laptop", 2,
                                    new BigDecimal("1000.00"), new BigDecimal("2000.00")));
        testItems.add(new OrderItem(null, 2L, "Mouse", 3,
                                    new BigDecimal("50.00"), new BigDecimal("150.00")));
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        @DisplayName("Should throw exception when customer ID is null")
        void shouldThrowExceptionWhenCustomerIdIsNull() {
            assertThatThrownBy(() -> new Order(1L, null, OrderStatus.PENDING, null, null, null,
                                              OffsetDateTime.now(), testAddress, testItems))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer ID cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when shipping address is null")
        void shouldThrowExceptionWhenShippingAddressIsNull() {
            assertThatThrownBy(() -> new Order(1L, 100L, OrderStatus.PENDING, null, null, null,
                                              OffsetDateTime.now(), null, testItems))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shipping address cannot be null");
        }

        @Test
        @DisplayName("Should create order with valid data")
        void shouldCreateOrderWithValidData() {
            Order order = new Order(1L, 100L, OrderStatus.PENDING,
                                   new BigDecimal("2150.00"), new BigDecimal("172.00"),
                                   new BigDecimal("2322.00"), OffsetDateTime.now(),
                                   testAddress, testItems);

            assertThat(order).isNotNull();
            assertThat(order.getId()).isEqualTo(1L);
            assertThat(order.getCustomerId()).isEqualTo(100L);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getItems()).hasSize(2);
        }

        @Test
        @DisplayName("Should default status to PENDING if null")
        void shouldDefaultStatusToPending() {
            Order order = new Order(1L, 100L, null, null, null, null,
                                   OffsetDateTime.now(), testAddress, testItems);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should default createdAt to now if null")
        void shouldDefaultCreatedAtToNow() {
            OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
            Order order = new Order(1L, 100L, null, null, null, null,
                                   null, testAddress, testItems);
            OffsetDateTime after = OffsetDateTime.now().plusSeconds(1);

            assertThat(order.getCreatedAt()).isAfter(before);
            assertThat(order.getCreatedAt()).isBefore(after);
        }
    }

    @Nested
    @DisplayName("Total Calculation")
    class TotalCalculation {

        @Test
        @DisplayName("Should calculate totals correctly when not provided")
        void shouldCalculateTotalsWhenNotProvided() {
            Order order = new Order(null, 100L, OrderStatus.PENDING, null, null, null,
                                   OffsetDateTime.now(), testAddress, testItems);

            // Subtotal: 2000.00 + 150.00 = 2150.00
            assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("2150.00"));
            // Tax: 2150.00 * 0.08 = 172.00
            assertThat(order.getTax()).isEqualByComparingTo(new BigDecimal("172.00"));
            // Total: 2150.00 + 172.00 = 2322.00
            assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("2322.00"));
        }

        @Test
        @DisplayName("Should use provided totals when given")
        void shouldUseProvidedTotals() {
            Order order = new Order(1L, 100L, OrderStatus.PENDING,
                                   new BigDecimal("1000.00"), new BigDecimal("80.00"),
                                   new BigDecimal("1080.00"), OffsetDateTime.now(),
                                   testAddress, testItems);

            assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(order.getTax()).isEqualByComparingTo(new BigDecimal("80.00"));
            assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("1080.00"));
        }

        @Test
        @DisplayName("Should calculate totals with empty items list")
        void shouldCalculateTotalsWithEmptyItems() {
            Order order = new Order(null, 100L, OrderStatus.PENDING, null, null, null,
                                   OffsetDateTime.now(), testAddress, new ArrayList<>());

            assertThat(order.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(order.getTax()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(order.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Factory Method")
    class FactoryMethod {

        @Test
        @DisplayName("Should create order with factory method")
        void shouldCreateOrderWithFactory() {
            Order order = Order.create(100L, testItems, testAddress);

            assertThat(order.getId()).isNull();
            assertThat(order.getCustomerId()).isEqualTo(100L);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getShippingAddress()).isEqualTo(testAddress);
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("2150.00"));
            assertThat(order.getTax()).isEqualByComparingTo(new BigDecimal("172.00"));
            assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("2322.00"));
        }

        @Test
        @DisplayName("Should set createdAt to current time in factory method")
        void shouldSetCreatedAtInFactory() {
            OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
            Order order = Order.create(100L, testItems, testAddress);
            OffsetDateTime after = OffsetDateTime.now().plusSeconds(1);

            assertThat(order.getCreatedAt()).isAfter(before);
            assertThat(order.getCreatedAt()).isBefore(after);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("Should return defensive copy of items list")
        void shouldReturnDefensiveCopyOfItems() {
            Order order = Order.create(100L, testItems, testAddress);
            List<OrderItem> returnedItems = order.getItems();

            assertThat(returnedItems).hasSize(2);

            // Attempt to modify returned list should not affect order
            returnedItems.clear();

            assertThat(order.getItems()).hasSize(2);
        }
    }
}