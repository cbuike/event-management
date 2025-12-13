package com.eventmanagement.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void testCategoryBuilder() {
        // Arrange
        Category category = Category.builder()
                .id(1L)
                .label("Category 1")
                .build();

        // Assert
        assertEquals(1, category.getId());
        assertEquals("Category 1", category.getLabel());
    }

    @Test
    void testNoArgsConstructor() {
        // Arrange
        Category category = new Category();

        // Assert
        assertNotNull( category);
    }

    @Test
    void testALlArgsConstructor() {
        // Arrange
        Category category = new Category(1L, "Category 1", null, List.of());

        // Assert
        assertEquals(1, category.getId());
        assertEquals("Category 1", category.getLabel());
        assertEquals(0, category.getChildren().size());
    }

}
