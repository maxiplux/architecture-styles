package app.quantun.architecture.adapter.in.web;

import app.quantun.architecture.adapter.in.web.dto.ProductFilterRequest;
import app.quantun.architecture.adapter.in.web.dto.ProductResponse;
import app.quantun.architecture.adapter.in.web.mapper.ProductWebMapper;
import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import app.quantun.architecture.application.port.in.SearchProductsUseCase;
import app.quantun.architecture.domain.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Product catalog operations")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final SearchProductsUseCase searchProductsUseCase;
    private final ProductWebMapper mapper;

    @Operation(summary = "Get products with dynamic filtering")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
        @ParameterObject ProductFilterRequest filter,
        @ParameterObject Pageable pageable) {

        ProductSearchCriteria criteria = mapper.toCriteria(filter);
        
        String sortBy = pageable.getSort().stream()
            .findFirst()
            .map(order -> order.getProperty())
            .orElse("id");
            
        String sortDirection = pageable.getSort().stream()
            .findFirst()
            .map(order -> order.getDirection().name())
            .orElse("ASC");

        Page<Product> products = searchProductsUseCase.searchProducts(
            criteria, 
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            sortBy, 
            sortDirection
        );

        Page<ProductResponse> response = products.map(mapper::toResponse);
        return ResponseEntity.ok(response);
    }
}
