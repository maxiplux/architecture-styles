package app.quantun.architecture.web;

import app.quantun.architecture.dto.CategoryDTO;
import app.quantun.architecture.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Categories", description = "Product category management endpoints for browsing and organizing products by categories")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

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
                            array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class)),
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
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }
}
