package app.quantun.architecture.adapter.in.web;

import app.quantun.architecture.adapter.in.web.mapper.CategoryWebMapper;
import app.quantun.architecture.application.port.in.GetCategoriesUseCase;
import app.quantun.architecture.domain.model.Category;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetCategoriesUseCase getCategoriesUseCase;

    @MockBean
    private CategoryWebMapper categoryWebMapper;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();
        category1 = new Category(1L, "Electronics", "Electronic devices and gadgets", true, now);
        category2 = new Category(2L, "Books", "Books and magazines", true, now);
    }

    @Test
    @DisplayName("Should return all active categories")
    void shouldReturnAllActiveCategories() throws Exception {
        // Given
        when(getCategoriesUseCase.getAllActiveCategories()).thenReturn(List.of(category1, category2));
        when(categoryWebMapper.toResponse(any())).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            return new app.quantun.architecture.adapter.in.web.dto.CategoryResponse(
                cat.getId(), cat.getName(), cat.getDescription(), cat.isActive(), cat.getCreatedAt()
            );
        });

        // When & Then
        mockMvc.perform(get("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[0].description").value("Electronic devices and gadgets"))
            .andExpect(jsonPath("$[0].active").value(true))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Books"))
            .andExpect(jsonPath("$[1].active").value(true));

        verify(getCategoriesUseCase, times(1)).getAllActiveCategories();
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void shouldReturnEmptyListWhenNoCategoriesExist() throws Exception {
        // Given
        when(getCategoriesUseCase.getAllActiveCategories()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(getCategoriesUseCase, times(1)).getAllActiveCategories();
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    void shouldHandleServiceException() throws Exception {
        // Given
        when(getCategoriesUseCase.getAllActiveCategories())
            .thenThrow(new RuntimeException("Database connection error"));

        // When & Then - Spring Boot default error handling returns 500
        mockMvc.perform(get("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is5xxServerError());

        verify(getCategoriesUseCase, times(1)).getAllActiveCategories();
    }
}