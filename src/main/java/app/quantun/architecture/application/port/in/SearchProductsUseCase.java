package app.quantun.architecture.application.port.in;

import app.quantun.architecture.domain.model.Product;
import org.springframework.data.domain.Page;

public interface SearchProductsUseCase {
    Page<Product> searchProducts(ProductSearchCriteria criteria, int page, int size, String sortBy, String sortDirection);
}