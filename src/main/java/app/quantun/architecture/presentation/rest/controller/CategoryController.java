package app.quantun.architecture.presentation.rest.controller;

import app.quantun.architecture.presentation.rest.dto.response.CategoryResponse;
import app.quantun.architecture.presentation.presenter.CategoryPresenter;
import app.quantun.architecture.usecase.category.CategoryOutputData;
import app.quantun.architecture.usecase.category.GetAllCategoriesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Categories", description = "Product category management endpoints for browsing and organizing products by categories")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final GetAllCategoriesUseCase getAllCategoriesUseCase;
    private final CategoryPresenter presenter;

    public CategoryController(GetAllCategoriesUseCase getAllCategoriesUseCase, CategoryPresenter presenter) {
        this.getAllCategoriesUseCase = getAllCategoriesUseCase;
        this.presenter = presenter;
    }

    @Operation(
            summary = "Get all product categories",
            description = "Retrieves a complete list of all available product categories in the catalog. " +
                    "Categories help organize products into logical groups for easier browsing. " +
                    "Each category includes its unique identifier, name, and description."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all categories",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class)),
                            examples = @ExampleObject(
                                    name = "Category List",
                                    summary = "Example response with multiple categories",
                                    value = """
                                            [
                                              {
                                                "id": 1,
                                                "name": "Electronics",
                                                "description": "Electronic devices and accessories"
                                              },
                                              {
                                                "id": 2,
                                                "name": "Clothing",
                                                "description": "Fashion and apparel items"
                                              },
                                              {
                                                "id": 3,
                                                "name": "Books",
                                                "description": "Physical and digital books"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Unable to retrieve categories",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-11-24T16:36:00.000+00:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Unable to retrieve categories",
                                              "path": "/api/v1/categories"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<CategoryOutputData> categories = getAllCategoriesUseCase.execute();
        List<CategoryResponse> response = presenter.present(categories);
        return ResponseEntity.ok(response);
    }
}