package app.quantun.architecture.web;

import app.quantun.architecture.dto.ProductDTO;
import app.quantun.architecture.dto.ProductFilter;
import app.quantun.architecture.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Product catalog management endpoints for searching, filtering, and browsing products with advanced query capabilities")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Search and filter products",
            description = "Retrieves a paginated list of products with advanced filtering capabilities. " +
                    "Supports filtering by category, name (partial match), price range, stock availability, and active status. " +
                    "Results are paginated and can be sorted by any product field. " +
                    "Combine multiple filters to narrow down search results precisely."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved filtered products with pagination",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Product Page",
                                    summary = "Example paginated response with filtered products",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "name": "Wireless Bluetooth Headphones",
                                                  "description": "Premium noise-canceling headphones with 30-hour battery life",
                                                  "price": 149.99,
                                                  "stockQuantity": 45,
                                                  "categoryId": 1,
                                                  "categoryName": "Electronics",
                                                  "active": true,
                                                  "imageUrl": "https://example.com/images/headphones.jpg"
                                                },
                                                {
                                                  "id": 2,
                                                  "name": "Smart LED TV 55 inch",
                                                  "description": "4K Ultra HD Smart TV with HDR support",
                                                  "price": 599.99,
                                                  "stockQuantity": 12,
                                                  "categoryId": 1,
                                                  "categoryName": "Electronics",
                                                  "active": true,
                                                  "imageUrl": "https://example.com/images/tv.jpg"
                                                }
                                              ],
                                              "pageable": {
                                                "pageNumber": 0,
                                                "pageSize": 20,
                                                "sort": {
                                                  "sorted": true,
                                                  "orders": [{"property": "name", "direction": "ASC"}]
                                                }
                                              },
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "size": 20,
                                              "number": 0,
                                              "first": true,
                                              "last": true,
                                              "empty": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid filter parameters",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-11-24T16:36:00.000+00:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid price range: minPrice cannot be greater than maxPrice",
                                              "path": "/api/v1/products"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-11-24T16:36:00.000+00:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Unable to retrieve products",
                                              "path": "/api/v1/products"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @Parameter(
                    description = "Filter products by category ID. Only products belonging to this category will be returned.",
                    example = "1"
            )
            @RequestParam(required = false) Long categoryId,
            
            @Parameter(
                    description = "Filter products by name (case-insensitive partial match). Searches for products containing this text in their name.",
                    example = "wireless"
            )
            @RequestParam(required = false) String name,
            
            @Parameter(
                    description = "Minimum price filter. Only products with price greater than or equal to this value will be returned.",
                    example = "10.00"
            )
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            
            @Parameter(
                    description = "Maximum price filter. Only products with price less than or equal to this value will be returned.",
                    example = "500.00"
            )
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            
            @Parameter(
                    description = "Filter by stock availability. Set to true to show only in-stock products, false for out-of-stock products.",
                    example = "true"
            )
            @RequestParam(required = false) Boolean inStock,
            
            @Parameter(
                    description = "Filter by active status. Set to true to show only active products (default), false to show inactive products.",
                    example = "true"
            )
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            
            @ParameterObject Pageable pageable
    ) {
        ProductFilter filter = new ProductFilter();
        filter.setCategoryId(categoryId);
        filter.setName(name);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setInStock(inStock);
        filter.setActive(active);
        return ResponseEntity.ok(productService.search(filter, pageable));
    }
}
