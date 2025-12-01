package app.quantun.architecture.presentation.rest.controller;

import app.quantun.architecture.presentation.rest.dto.response.PageResponse;
import app.quantun.architecture.presentation.rest.dto.response.ProductResponse;
import app.quantun.architecture.presentation.presenter.ProductPresenter;
import app.quantun.architecture.usecase.product.PagedProductOutputData;
import app.quantun.architecture.usecase.product.ProductSearchCriteria;
import app.quantun.architecture.usecase.product.SearchProductsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Products", description = "Product catalog management endpoints for searching, filtering, and browsing products with advanced query capabilities")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final SearchProductsUseCase searchProductsUseCase;
    private final ProductPresenter presenter;

    public ProductController(SearchProductsUseCase searchProductsUseCase, ProductPresenter presenter) {
        this.searchProductsUseCase = searchProductsUseCase;
        this.presenter = presenter;
    }

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
                            schema = @Schema(implementation = PageResponse.class),
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
                                                  "stock": 45,
                                                  "categoryId": 1,
                                                  "categoryName": "Electronics",
                                                  "active": true,
                                                  "imageUrl": "https://example.com/images/headphones.jpg"
                                                }
                                              ],
                                              "totalElements": 1,
                                              "totalPages": 1,
                                              "number": 0,
                                              "size": 20,
                                              "first": true,
                                              "last": true
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
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
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
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(
                    description = "Maximum price filter. Only products with price less than or equal to this value will be returned.",
                    example = "500.00"
            )
            @RequestParam(required = false) BigDecimal maxPrice,

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

            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") int size,

            @Parameter(description = "Sort field", example = "name")
            @RequestParam(required = false, defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(required = false, defaultValue = "asc") String sortDirection
    ) {
        ProductSearchCriteria criteria = ProductSearchCriteria.of(
                categoryId, name, minPrice, maxPrice, inStock, active,
                page, size, sortBy, sortDirection
        );

        PagedProductOutputData output = searchProductsUseCase.execute(criteria);
        PageResponse<ProductResponse> response = presenter.present(output);

        return ResponseEntity.ok(response);
    }
}