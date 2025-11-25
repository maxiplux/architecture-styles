package app.quantun.architecture.domain.model;

import app.quantun.architecture.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product Domain Model Tests")
class ProductTest {

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        testProduct = new Product(1L, "Laptop", "High-end laptop", new BigDecimal("999.99"),
                                  10, "http://image.url", true, testCategory);
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        @DisplayName("Should throw exception when product name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> new Product(1L, null, "Description", new BigDecimal("100"),
                                                10, "url", true, testCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name cannot be blank");
        }

        @Test
        @DisplayName("Should throw exception when product name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> new Product(1L, "  ", "Description", new BigDecimal("100"),
                                                10, "url", true, testCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name cannot be blank");
        }

        @Test
        @DisplayName("Should throw exception when price is null")
        void shouldThrowExceptionWhenPriceIsNull() {
            assertThatThrownBy(() -> new Product(1L, "Product", "Description", null,
                                                10, "url", true, testCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product price must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when price is negative")
        void shouldThrowExceptionWhenPriceIsNegative() {
            assertThatThrownBy(() -> new Product(1L, "Product", "Description", new BigDecimal("-10"),
                                                10, "url", true, testCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product price must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when stock is negative")
        void shouldThrowExceptionWhenStockIsNegative() {
            assertThatThrownBy(() -> new Product(1L, "Product", "Description", new BigDecimal("100"),
                                                -5, "url", true, testCategory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product stock must be non-negative");
        }

        @Test
        @DisplayName("Should create product with valid data")
        void shouldCreateProductWithValidData() {
            Product product = new Product(1L, "Valid Product", "Description", new BigDecimal("99.99"),
                                         5, "url", true, testCategory);

            assertThat(product).isNotNull();
            assertThat(product.getName()).isEqualTo("Valid Product");
            assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(product.getStock()).isEqualTo(5);
            assertThat(product.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Stock Management")
    class StockManagement {

        @Test
        @DisplayName("Should correctly identify when product is in stock")
        void shouldIdentifyInStock() {
            assertThat(testProduct.isInStock()).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify when product is out of stock")
        void shouldIdentifyOutOfStock() {
            Product outOfStock = new Product(1L, "Product", "Desc", new BigDecimal("100"),
                                            0, "url", true, testCategory);
            assertThat(outOfStock.isInStock()).isFalse();
        }

        @Test
        @DisplayName("Should correctly check if has enough stock")
        void shouldCheckEnoughStock() {
            assertThat(testProduct.hasEnoughStock(5)).isTrue();
            assertThat(testProduct.hasEnoughStock(10)).isTrue();
            assertThat(testProduct.hasEnoughStock(11)).isFalse();
        }

        @Test
        @DisplayName("Should decrement stock successfully when sufficient")
        void shouldDecrementStockSuccessfully() {
            testProduct.decrementStock(3);
            assertThat(testProduct.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw exception when decrementing stock with insufficient quantity")
        void shouldThrowExceptionWhenInsufficientStock() {
            assertThatThrownBy(() -> testProduct.decrementStock(15))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Product 1 has insufficient stock")
                .hasMessageContaining("Available: 10")
                .hasMessageContaining("Requested: 15");
        }

        @Test
        @DisplayName("Should allow decrementing to zero stock")
        void shouldAllowDecrementingToZero() {
            testProduct.decrementStock(10);
            assertThat(testProduct.getStock()).isEqualTo(0);
            assertThat(testProduct.isInStock()).isFalse();
        }
    }

    @Nested
    @DisplayName("Factory Method")
    class FactoryMethod {

        @Test
        @DisplayName("Should create product with null ID using factory method")
        void shouldCreateProductWithNullIdUsingFactory() {
            Product product = Product.create("New Product", "Description",
                                            new BigDecimal("199.99"), 20, "url", testCategory);

            assertThat(product.getId()).isNull();
            assertThat(product.getName()).isEqualTo("New Product");
            assertThat(product.isActive()).isTrue();
            assertThat(product.getStock()).isEqualTo(20);
        }
    }
}