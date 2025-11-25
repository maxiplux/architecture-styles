package app.quantun.architecture.service;

import app.quantun.architecture.domain.Category;
import app.quantun.architecture.dto.CategoryDTO;
import app.quantun.architecture.mapper.CategoryMapper;
import app.quantun.architecture.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAll() {
        Category category = new Category(1L, "Electronics", "Gadgets", true, OffsetDateTime.now());
        CategoryDTO categoryDTO = new CategoryDTO(1L, "Electronics", "Gadgets", true, OffsetDateTime.now());

        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(category));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryDTO);

        List<CategoryDTO> result = categoryService.findAll();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
    }
}
