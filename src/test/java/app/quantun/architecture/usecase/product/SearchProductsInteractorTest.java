package app.quantun.architecture.usecase.product;

import app.quantun.architecture.entity.Category;
import app.quantun.architecture.entity.Product;
import app.quantun.architecture.shared.exception.EntityNotFoundException;
import app.quantun.architecture.usecase.gateway.CategoryGateway;
import app.quantun.architecture.usecase.gateway.PagedResult;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchProductsInteractorTest {

    @Mock
    private ProductGateway productGateway;

    @Mock
    private CategoryGateway categoryGateway;

    private SearchProductsInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new SearchProductsInteractor(productGateway, categoryGateway);
    }

    @Test
    void execute_withValidCriteria_returnsProducts() {
        Category category = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        Product product = Product.create(1L, "Laptop", "Gaming laptop", new BigDecimal("1299.99"),
                10, "laptop.jpg", true, category);

        ProductSearchCriteria criteria = ProductSearchCriteria.of(
                null, null, null, null, null, true, 0, 20, "id", "asc"
        );

        when(productGateway.findAll(any(ProductSearchCriteria.class)))
                .thenReturn(new PagedResult<>(List.of(product), 1, 1, 0, 20));

        PagedProductOutputData result = interactor.execute(criteria);

        assertEquals(1, result.content().size());
        assertEquals("Laptop", result.content().get(0).name());
        assertEquals(1, result.totalElements());
        verify(productGateway).findAll(criteria);
    }

    @Test
    void execute_withCategoryFilter_validatesCategoryExists() {
        ProductSearchCriteria criteria = ProductSearchCriteria.of(
                1L, null, null, null, null, true, 0, 20, "id", "asc"
        );

        when(categoryGateway.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> interactor.execute(criteria));
        verify(categoryGateway).existsById(1L);
        verify(productGateway, never()).findAll(any());
    }

    @Test
    void execute_withEmptyResult_returnsEmptyPage() {
        ProductSearchCriteria criteria = ProductSearchCriteria.of(
                null, "NonExistent", null, null, null, true, 0, 20, "id", "asc"
        );

        when(productGateway.findAll(any(ProductSearchCriteria.class)))
                .thenReturn(new PagedResult<>(List.of(), 0, 0, 0, 20));

        PagedProductOutputData result = interactor.execute(criteria);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }
}
