package app.quantun.architecture.usecase.product;

/**
 * Input boundary (use case interface) for searching products.
 */
public interface SearchProductsUseCase {
    PagedProductOutputData execute(ProductSearchCriteria criteria);
}
