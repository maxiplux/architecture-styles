package app.quantun.architecture.service;

import app.quantun.architecture.domain.Category;
import app.quantun.architecture.domain.Product;
import app.quantun.architecture.dto.ProductDTO;
import app.quantun.architecture.dto.ProductFilter;
import app.quantun.architecture.mapper.ProductMapper;
import app.quantun.architecture.repository.CategoryRepository;
import app.quantun.architecture.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void search() {
        ProductFilter filter = new ProductFilter();
        filter.setCategoryId(1L);

        Category category = Category.builder().id(1L).name("Cat").build();
        Product product = Product.builder().id(1L).name("Prod").category(category).price(BigDecimal.TEN).build();
        ProductDTO productDTO = new ProductDTO(1L, "Prod", null, BigDecimal.TEN, 1L, "Cat", 10, null, true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDTO);

        Page<ProductDTO> result = productService.search(filter, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Prod", result.getContent().get(0).name());
    }
}
