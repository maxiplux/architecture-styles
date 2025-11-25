package app.quantun.architecture.application.service;

import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import app.quantun.architecture.application.port.out.CategoryRepositoryPort;
import app.quantun.architecture.application.port.out.ProductRepositoryPort;
import app.quantun.architecture.domain.exception.CategoryNotFoundException;
import app.quantun.architecture.domain.model.Category;
import app.quantun.architecture.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Implementation Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category testCategory;
    private Product product1;
    private Product product2;
    private Page<Product> productPage;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Electronics", "Electronic items", true, OffsetDateTime.now());

        product1 = new Product(1L, "Laptop", "High-end laptop",
                              new BigDecimal("1000.00"), 10, "url1", true, testCategory);

        product2 = new Product(2L, "Mouse", "Wireless mouse",
                              new BigDecimal("50.00"), 20, "url2", true, testCategory);

        productPage = new PageImpl<>(List.of(product1, product2));
    }

    @Nested
    @DisplayName("Product Search")
    class ProductSearch {

        @Test
        @DisplayName("Should search products without category filter")
        void shouldSearchProductsWithoutCategoryFilter() {
            // Given
            ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, "laptop", new BigDecimal("100"), new BigDecimal("2000"), true, true
            );
            when(productRepository.findAll(eq(criteria), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(productPage);

            // When
            Page<Product> result = productService.searchProducts(criteria, 0, 10, "name", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).contains(product1, product2);
            verify(categoryRepository, never()).existsById(anyLong());
        }

        @Test
        @DisplayName("Should validate category exists when category filter provided")
        void shouldValidateCategoryExists() {
            // Given
            ProductSearchCriteria criteria = new ProductSearchCriteria(
                1L, null, null, null, null, true
            );
            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findAll(eq(criteria), eq(0), eq(20), eq("id"), eq("desc")))
                .thenReturn(productPage);

            // When
            Page<Product> result = productService.searchProducts(criteria, 0, 20, "id", "desc");

            // Then
            assertThat(result).isNotNull();
            verify(categoryRepository).existsById(1L);
            verify(productRepository).findAll(criteria, 0, 20, "id", "desc");
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Given
            ProductSearchCriteria criteria = new ProductSearchCriteria(
                999L, null, null, null, null, true
            );
            when(categoryRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> productService.searchProducts(criteria, 0, 10, "name", "asc"))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found: 999");

            verify(productRepository, never()).findAll(any(), anyInt(), anyInt(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Search Criteria Handling")
    class SearchCriteriaHandling {

        @Test
        @DisplayName("Should handle all filter parameters")
        void shouldHandleAllFilterParameters() {
            // Given
            ProductSearchCriteria criteria = new ProductSearchCriteria(
                1L, "laptop", new BigDecimal("500"), new BigDecimal("1500"), true, true
            );
            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findAll(any(ProductSearchCriteria.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(productPage);

            // When
            Page<Product> result = productService.searchProducts(criteria, 0, 10, "price", "asc");

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).findAll(criteria, 0, 10, "price", "asc");
        }

        @Test
        @DisplayName("Should handle empty search criteria")
        void shouldHandleEmptySearchCriteria() {
            // Given
            ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, null, null, null
            );
            when(productRepository.findAll(any(ProductSearchCriteria.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(productPage);

            // When
            Page<Product> result = productService.searchProducts(criteria, 0, 10, "name", "asc");

            // Then
            assertThat(result).isNotNull();
            // Active should default to true in the criteria constructor
            assertThat(criteria.active()).isTrue();
        }

        @Test
        @DisplayName("Should pass through pagination parameters correctly")
        void shouldPassThroughPaginationParameters() {
            // Given
            ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, null, null, true);
            when(productRepository.findAll(any(ProductSearchCriteria.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(product1), org.springframework.data.domain.PageRequest.of(2, 5), 100));

            // When
            Page<Product> result = productService.searchProducts(criteria, 2, 5, "stock", "desc");

            // Then
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(100);
            verify(productRepository).findAll(criteria, 2, 5, "stock", "desc");
        }
    }

    @Nested
    @DisplayName("Empty Results")
    class EmptyResults {

        @Test
        @DisplayName("Should handle empty search results")
        void shouldHandleEmptySearchResults() {
            // Given
            ProductSearchCriteria criteria = new ProductSearchCriteria(null, "nonexistent", null, null, null, true);
            Page<Product> emptyPage = new PageImpl<>(List.of());
            when(productRepository.findAll(any(ProductSearchCriteria.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(emptyPage);

            // When
            Page<Product> result = productService.searchProducts(criteria, 0, 10, "name", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }
}