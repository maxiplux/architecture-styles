package app.quantun.architecture.adapter.in.web;

import app.quantun.architecture.adapter.in.web.dto.ProductFilterRequest;
import app.quantun.architecture.adapter.in.web.dto.ProductResponse;
import app.quantun.architecture.adapter.in.web.mapper.ProductWebMapper;
import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import app.quantun.architecture.application.port.in.SearchProductsUseCase;
import app.quantun.architecture.domain.model.Category;
import app.quantun.architecture.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SearchProductsUseCase searchProductsUseCase;
    @MockBean private ProductWebMapper productWebMapper;

    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Electronics", "Desc", true, OffsetDateTime.now());
        // Constructor: id, name, description, price, stock, imageUrl, active, category
        product = new Product(101L, "Headphones", "Desc", new BigDecimal("149.99"), 50, "url", true, category);
        
        productResponse = new ProductResponse(
            101L, "Headphones", "Desc", new BigDecimal("149.99"), 
            1L, "Electronics", 50, "url", true
        );
    }

    @Test
    @DisplayName("Should return paginated products")
    void shouldReturnPaginatedProducts() throws Exception {
        Page<Product> productPage = new PageImpl<>(List.of(product));
        
        when(productWebMapper.toCriteria(any())).thenReturn(
            new ProductSearchCriteria(null, null, null, null, null, null)
        );
        when(searchProductsUseCase.searchProducts(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(productPage);
        when(productWebMapper.toResponse(any())).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(101))
            .andExpect(jsonPath("$.content[0].name").value("Headphones"));
    }
}
