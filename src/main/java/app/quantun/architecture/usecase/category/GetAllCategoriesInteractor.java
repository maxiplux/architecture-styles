package app.quantun.architecture.usecase.category;

import app.quantun.architecture.entity.Category;
import app.quantun.architecture.usecase.gateway.CategoryGateway;

import java.util.List;

/**
 * Interactor implementing the GetAllCategories use case.
 * Pure Java class with no framework annotations.
 */
public class GetAllCategoriesInteractor implements GetAllCategoriesUseCase {

    private final CategoryGateway categoryGateway;

    public GetAllCategoriesInteractor(CategoryGateway categoryGateway) {
        this.categoryGateway = categoryGateway;
    }

    @Override
    public List<CategoryOutputData> execute() {
        return categoryGateway.findAllActive().stream()
                .map(this::toOutputData)
                .toList();
    }

    private CategoryOutputData toOutputData(Category category) {
        return new CategoryOutputData(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
