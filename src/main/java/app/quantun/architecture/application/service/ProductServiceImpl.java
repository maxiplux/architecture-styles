package app.quantun.architecture.application.service;

import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import app.quantun.architecture.application.port.in.SearchProductsUseCase;
import app.quantun.architecture.application.port.out.CategoryRepositoryPort;
import app.quantun.architecture.application.port.out.ProductRepositoryPort;
import app.quantun.architecture.domain.exception.CategoryNotFoundException;
import app.quantun.architecture.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements SearchProductsUseCase {

    private final ProductRepositoryPort productRepository;
    private final CategoryRepositoryPort categoryRepository;

    @Override
    public Page<Product> searchProducts(ProductSearchCriteria criteria, int page, int size,
                                         String sortBy, String sortDirection) {
        // Validate category exists if specified
        if (criteria.categoryId() != null && !categoryRepository.existsById(criteria.categoryId())) {
            throw new CategoryNotFoundException("Category not found: " + criteria.categoryId());
        }

        return productRepository.findAll(criteria, page, size, sortBy, sortDirection);
    }
}