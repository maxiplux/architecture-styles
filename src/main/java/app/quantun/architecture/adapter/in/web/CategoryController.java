package app.quantun.architecture.adapter.in.web;

import app.quantun.architecture.adapter.in.web.dto.CategoryResponse;
import app.quantun.architecture.adapter.in.web.mapper.CategoryWebMapper;
import app.quantun.architecture.application.port.in.GetCategoriesUseCase;
import app.quantun.architecture.domain.model.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Categories", description = "Product category management endpoints")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final GetCategoriesUseCase getCategoriesUseCase;
    private final CategoryWebMapper mapper;

    @Operation(summary = "Get all product categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all categories"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<Category> categories = getCategoriesUseCase.getAllActiveCategories();
        List<CategoryResponse> response = categories.stream()
            .map(mapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }
}