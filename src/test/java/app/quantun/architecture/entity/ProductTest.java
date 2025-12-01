package app.quantun.architecture.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Category createCategory() {
        return Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
    }

    @Test
    void create_withValidData_createsProduct() {
        Category category = createCategory();
        Product product = Product.create(1L, "Gaming Laptop", "High-performance laptop",
                new BigDecimal("1299.99"), 10, "laptop.jpg", true, category);

        assertNotNull(product);
        assertEquals("Gaming Laptop", product.getName());
        assertEquals(new BigDecimal("1299.99"), product.getPrice());
        assertEquals(10, product.getStock());
        assertTrue(product.isActive());
    }

    @Test
    void create_withNullName_throwsException() {
        Category category = createCategory();
        assertThrows(IllegalArgumentException.class, () ->
                Product.create(1L, null, "Description", BigDecimal.TEN, 5, "img.jpg", true, category)
        );
    }

    @Test
    void create_withNegativePrice_throwsException() {
        Category category = createCategory();
        assertThrows(IllegalArgumentException.class, () ->
                Product.create(1L, "Product", "Description", new BigDecimal("-10"), 5, "img.jpg", true, category)
        );
    }

    @Test
    void create_withNegativeStock_throwsException() {
        Category category = createCategory();
        assertThrows(IllegalArgumentException.class, () ->
                Product.create(1L, "Product", "Description", BigDecimal.TEN, -1, "img.jpg", true, category)
        );
    }

    @Test
    void isAvailable_withActiveAndStock_returnsTrue() {
        Category category = createCategory();
        Product product = Product.create(1L, "Product", "Description", BigDecimal.TEN, 10, "img.jpg", true, category);

        assertTrue(product.isAvailable());
    }

    @Test
    void isAvailable_withZeroStock_returnsFalse() {
        Category category = createCategory();
        Product product = Product.create(1L, "Product", "Description", BigDecimal.TEN, 0, "img.jpg", true, category);

        assertFalse(product.isAvailable());
    }

    @Test
    void isAvailable_withInactive_returnsFalse() {
        Category category = createCategory();
        Product product = Product.create(1L, "Product", "Description", BigDecimal.TEN, 10, "img.jpg", false, category);

        assertFalse(product.isAvailable());
    }

    @Test
    void canFulfillQuantity_withSufficientStock_returnsTrue() {
        Category category = createCategory();
        Product product = Product.create(1L, "Product", "Description", BigDecimal.TEN, 10, "img.jpg", true, category);

        assertTrue(product.canFulfillQuantity(5));
    }

    @Test
    void canFulfillQuantity_withInsufficientStock_returnsFalse() {
        Category category = createCategory();
        Product product = Product.create(1L, "Product", "Description", BigDecimal.TEN, 5, "img.jpg", true, category);

        assertFalse(product.canFulfillQuantity(10));
    }

    @Test
    void reduceStock_withSufficientQuantity_reducesSuccessfully() {
        Category category = createCategory();
        Product product = Product.create(1L, "Laptop", "Desc", BigDecimal.TEN, 10, "img.jpg", true, category);

        product.reduceStock(3);

        assertEquals(7, product.getStock());
    }

    @Test
    void reduceStock_withInsufficientQuantity_throwsException() {
        Category category = createCategory();
        Product product = Product.create(1L, "Laptop", "Desc", BigDecimal.TEN, 5, "img.jpg", true, category);

        assertThrows(IllegalStateException.class, () -> product.reduceStock(10));
    }
}
