package app.quantun.architecture.usecase.product;

import app.quantun.architecture.entity.Product;
import app.quantun.architecture.shared.exception.EntityNotFoundException;
import app.quantun.architecture.usecase.gateway.CategoryGateway;
import app.quantun.architecture.usecase.gateway.PagedResult;
import app.quantun.architecture.usecase.gateway.ProductGateway;

import java.util.List;

/**
 * Interactor implementing the SearchProducts use case.
 * Pure Java class with no framework annotations.
 */
public class SearchProductsInteractor implements SearchProductsUseCase {

    private final ProductGateway productGateway;
    private final CategoryGateway categoryGateway;

    public SearchProductsInteractor(ProductGateway productGateway, CategoryGateway categoryGateway) {
        this.productGateway = productGateway;
        this.categoryGateway = categoryGateway;
    }

    @Override
    public PagedProductOutputData execute(ProductSearchCriteria criteria) {
        // Validate category exists if specified
        if (criteria.categoryId() != null && !categoryGateway.existsById(criteria.categoryId())) {
            throw new EntityNotFoundException("Category not found: " + criteria.categoryId());
        }

        PagedResult<Product> result = productGateway.findAll(criteria);

        List<ProductOutputData> content = result.content().stream()
                .map(this::toOutputData)
                .toList();

        return new PagedProductOutputData(
                content,
                result.totalElements(),
                result.totalPages(),
                result.number(),
                result.size(),
                result.isFirst(),
                result.isLast()
        );
    }

    private ProductOutputData toOutputData(Product product) {
        return new ProductOutputData(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getStock(),
                product.getImageUrl(),
                product.isActive()
        );
    }
}
