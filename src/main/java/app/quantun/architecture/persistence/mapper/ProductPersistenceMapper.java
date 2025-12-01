package app.quantun.architecture.persistence.mapper;

import app.quantun.architecture.entity.Category;
import app.quantun.architecture.entity.Product;
import app.quantun.architecture.persistence.entity.CategoryJpaEntity;
import app.quantun.architecture.persistence.entity.ProductJpaEntity;
import app.quantun.architecture.persistence.repository.CategoryJpaRepository;
import org.springframework.stereotype.Component;

/**
 * Mapper for transforming between Product domain entity and ProductJpaEntity JPA entity.
 */
@Component
public class ProductPersistenceMapper {

    private final CategoryPersistenceMapper categoryMapper;
    private final CategoryJpaRepository categoryRepository;

    public ProductPersistenceMapper(CategoryPersistenceMapper categoryMapper, CategoryJpaRepository categoryRepository) {
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
    }

    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Category category = categoryMapper.toDomain(entity.getCategory());
        return Product.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                entity.getImageUrl(),
                entity.isActive(),
                category
        );
    }

    public ProductJpaEntity toJpaEntity(Product product) {
        if (product == null) {
            return null;
        }

        CategoryJpaEntity categoryEntity = null;
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            categoryEntity = categoryRepository.findById(product.getCategory().getId())
                    .orElse(null);
        }

        return ProductJpaEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .category(categoryEntity)
                .build();
    }
}
