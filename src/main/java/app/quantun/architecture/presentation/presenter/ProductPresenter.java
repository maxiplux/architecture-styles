package app.quantun.architecture.presentation.presenter;

import app.quantun.architecture.presentation.rest.dto.response.PageResponse;
import app.quantun.architecture.presentation.rest.dto.response.ProductResponse;
import app.quantun.architecture.usecase.product.PagedProductOutputData;
import app.quantun.architecture.usecase.product.ProductOutputData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Presenter for transforming product use case output to HTTP response models.
 */
@Component
public class ProductPresenter {

    public PageResponse<ProductResponse> present(PagedProductOutputData output) {
        List<ProductResponse> content = output.content().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                output.totalElements(),
                output.totalPages(),
                output.number(),
                output.size(),
                output.first(),
                output.last()
        );
    }

    private ProductResponse toResponse(ProductOutputData data) {
        return new ProductResponse(
                data.id(),
                data.name(),
                data.description(),
                data.price(),
                data.categoryId(),
                data.categoryName(),
                data.stock(),
                data.imageUrl(),
                data.active()
        );
    }
}
