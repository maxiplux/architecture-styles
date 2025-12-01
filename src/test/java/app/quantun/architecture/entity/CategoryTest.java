package app.quantun.architecture.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void create_withValidData_createsCategory() {
        Category category = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());

        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Electronics", category.getName());
        assertEquals("Electronic devices", category.getDescription());
        assertTrue(category.isActive());
        assertNotNull(category.getCreatedAt());
    }

    @Test
    void create_withNullName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                Category.create(1L, null, "Description", true, OffsetDateTime.now())
        );
    }

    @Test
    void create_withBlankName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                Category.create(1L, "   ", "Description", true, OffsetDateTime.now())
        );
    }

    @Test
    void create_withNameExceedingMaxLength_throwsException() {
        String longName = "a".repeat(101);
        assertThrows(IllegalArgumentException.class, () ->
                Category.create(1L, longName, "Description", true, OffsetDateTime.now())
        );
    }

    @Test
    void create_withNullCreatedAt_usesCurrentTime() {
        Category category = Category.create(1L, "Electronics", "Description", true, null);

        assertNotNull(category.getCreatedAt());
    }
}
