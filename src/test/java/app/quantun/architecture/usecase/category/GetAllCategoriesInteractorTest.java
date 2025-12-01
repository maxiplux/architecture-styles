package app.quantun.architecture.usecase.category;

import app.quantun.architecture.entity.Category;
import app.quantun.architecture.usecase.gateway.CategoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllCategoriesInteractorTest {

    @Mock
    private CategoryGateway categoryGateway;

    private GetAllCategoriesInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new GetAllCategoriesInteractor(categoryGateway);
    }

    @Test
    void execute_withCategories_returnsAllCategories() {
        Category category1 = Category.create(1L, "Electronics", "Electronic devices", true, OffsetDateTime.now());
        Category category2 = Category.create(2L, "Clothing", "Fashion items", true, OffsetDateTime.now());
        when(categoryGateway.findAllActive()).thenReturn(List.of(category1, category2));

        List<CategoryOutputData> result = interactor.execute();

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertEquals("Clothing", result.get(1).name());
        verify(categoryGateway).findAllActive();
    }

    @Test
    void execute_withNoCategories_returnsEmptyList() {
        when(categoryGateway.findAllActive()).thenReturn(List.of());

        List<CategoryOutputData> result = interactor.execute();

        assertTrue(result.isEmpty());
        verify(categoryGateway).findAllActive();
    }
}
