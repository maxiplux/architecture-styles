package app.quantun.architecture.domain.model;

import app.quantun.architecture.domain.exception.InsufficientStockException;
import app.quantun.architecture.domain.exception.ProductNotActiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem Domain Model Tests")
class OrderItemTest {

    private Category testCategory;
    private Product activeProduct;
    private Product inactiveProduct;
    private Product lowStockProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());

        activeProduct = new Product(1L, "Laptop", "High-end laptop",
                                   new BigDecimal("999.99"), 10, "url", true, testCategory);

        inactiveProduct = new Product(2L, "Old Phone", "Discontinued phone",
                                     new BigDecimal("299.99"), 5, "url", false, testCategory);

        lowStockProduct = new Product(3L, "Tablet", "Limited stock tablet",
                                     new BigDecimal("599.99"), 2, "url", true, testCategory);
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        @DisplayName("Should throw exception when quantity is null")
        void shouldThrowExceptionWhenQuantityIsNull() {
            assertThatThrownBy(() -> new OrderItem(1L, 1L, "Product", null,
                                                   new BigDecimal("100"), new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero")
        void shouldThrowExceptionWhenQuantityIsZero() {
            assertThatThrownBy(() -> new OrderItem(1L, 1L, "Product", 0,
                                                   new BigDecimal("100"), new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            assertThatThrownBy(() -> new OrderItem(1L, 1L, "Product", -5,
                                                   new BigDecimal("100"), new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
        }

        @Test
        @DisplayName("Should throw exception when unit price is null")
        void shouldThrowExceptionWhenUnitPriceIsNull() {
            assertThatThrownBy(() -> new OrderItem(1L, 1L, "Product", 1, null, new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit price must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when unit price is negative")
        void shouldThrowExceptionWhenUnitPriceIsNegative() {
            assertThatThrownBy(() -> new OrderItem(1L, 1L, "Product", 1,
                                                   new BigDecimal("-50"), new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit price must be non-negative");
        }

        @Test
        @DisplayName("Should create order item with valid data")
        void shouldCreateOrderItemWithValidData() {
            OrderItem item = new OrderItem(1L, 100L, "Test Product", 3,
                                          new BigDecimal("99.99"), new BigDecimal("299.97"));

            assertThat(item).isNotNull();
            assertThat(item.getId()).isEqualTo(1L);
            assertThat(item.getProductId()).isEqualTo(100L);
            assertThat(item.getProductName()).isEqualTo("Test Product");
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("299.97"));
        }

        @Test
        @DisplayName("Should calculate subtotal when not provided")
        void shouldCalculateSubtotalWhenNotProvided() {
            OrderItem item = new OrderItem(null, 1L, "Product", 5,
                                          new BigDecimal("20.00"), null);

            assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should apply scale to monetary values")
        void shouldApplyScaleToMonetaryValues() {
            OrderItem item = new OrderItem(null, 1L, "Product", 3,
                                          new BigDecimal("10.333"), new BigDecimal("30.999"));

            assertThat(item.getUnitPrice().scale()).isEqualTo(2);
            assertThat(item.getSubtotal().scale()).isEqualTo(2);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("10.33"));
            assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("31.00"));
        }
    }

    @Nested
    @DisplayName("Factory Method")
    class FactoryMethod {

        @Test
        @DisplayName("Should create order item from active product with sufficient stock")
        void shouldCreateFromActiveProductWithStock() {
            OrderItem item = OrderItem.create(activeProduct, 3);

            assertThat(item).isNotNull();
            assertThat(item.getId()).isNull();
            assertThat(item.getProductId()).isEqualTo(1L);
            assertThat(item.getProductName()).isEqualTo("Laptop");
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
            assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("2999.97"));
        }

        @Test
        @DisplayName("Should throw exception when product is inactive")
        void shouldThrowExceptionWhenProductIsInactive() {
            assertThatThrownBy(() -> OrderItem.create(inactiveProduct, 1))
                .isInstanceOf(ProductNotActiveException.class)
                .hasMessage("Product is inactive: 2");
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            assertThatThrownBy(() -> OrderItem.create(lowStockProduct, 5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Product 3 has insufficient stock")
                .hasMessageContaining("Available: 2")
                .hasMessageContaining("Requested: 5");
        }

        @Test
        @DisplayName("Should allow ordering exactly available stock")
        void shouldAllowOrderingExactStock() {
            OrderItem item = OrderItem.create(lowStockProduct, 2);

            assertThat(item).isNotNull();
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("1199.98"));
        }
    }
}