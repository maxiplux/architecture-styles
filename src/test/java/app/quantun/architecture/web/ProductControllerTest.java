package app.quantun.architecture.web;

import app.quantun.architecture.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void shouldReturnBadRequest_whenNameIsTooLong() throws Exception {
        // Name > 100 chars
        String longName = "a".repeat(101);

        when(productService.search(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/products")
                        .param("name", longName))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenMinPriceIsNegative() throws Exception {
        when(productService.search(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/products")
                        .param("minPrice", "-10.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenMaxPriceIsNegative() throws Exception {
        when(productService.search(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/products")
                        .param("maxPrice", "-5.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenMinPriceGreaterThanMaxPrice() throws Exception {
        when(productService.search(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/products")
                        .param("minPrice", "100.00")
                        .param("maxPrice", "50.00"))
                .andExpect(status().isBadRequest());
    }
}
