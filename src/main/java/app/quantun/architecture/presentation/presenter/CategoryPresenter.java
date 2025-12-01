package app.quantun.architecture.presentation.presenter;

import app.quantun.architecture.presentation.rest.dto.response.CategoryResponse;
import app.quantun.architecture.usecase.category.CategoryOutputData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Presenter for transforming category use case output to HTTP response models.
 */
@Component
public class CategoryPresenter {

    public List<CategoryResponse> present(List<CategoryOutputData> categories) {
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(CategoryOutputData data) {
        return new CategoryResponse(
                data.id(),
                data.name(),
                data.description(),
                data.active(),
                data.createdAt()
        );
    }
}
