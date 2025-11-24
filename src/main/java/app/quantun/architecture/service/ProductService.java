package app.quantun.architecture.service;

import app.quantun.architecture.domain.Product;
import app.quantun.architecture.dto.ProductDTO;
import app.quantun.architecture.dto.ProductFilter;
import app.quantun.architecture.exception.NotFoundException;
import app.quantun.architecture.repository.CategoryRepository;
import app.quantun.architecture.repository.ProductRepository;
import app.quantun.architecture.spec.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductDTO> search(ProductFilter filter, Pageable pageable) {
        if (filter.getCategoryId() != null) {
            categoryRepository.findById(filter.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found: " + filter.getCategoryId()));
        }

        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.isActive(filter.getActive()),
                ProductSpecifications.hasCategory(filter.getCategoryId()),
                ProductSpecifications.nameLike(filter.getName()),
                ProductSpecifications.priceBetween(filter.getMinPrice(), filter.getMaxPrice()),
                ProductSpecifications.inStock(filter.getInStock())
        );

        return productRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    private ProductDTO toDto(Product p) {
        Long categoryId = p.getCategory() != null ? p.getCategory().getId() : null;
        String categoryName = p.getCategory() != null ? p.getCategory().getName() : null;
        return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                categoryId,
                categoryName,
                p.getStock(),
                p.getImageUrl(),
                p.isActive()
        );
    }
}
