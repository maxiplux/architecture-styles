package app.quantun.architecture.persistence.gateway;

import app.quantun.architecture.entity.Product;
import app.quantun.architecture.persistence.entity.ProductJpaEntity;
import app.quantun.architecture.persistence.mapper.ProductPersistenceMapper;
import app.quantun.architecture.persistence.repository.ProductJpaRepository;
import app.quantun.architecture.persistence.specification.ProductSpecifications;
import app.quantun.architecture.usecase.gateway.PagedResult;
import app.quantun.architecture.usecase.gateway.ProductGateway;
import app.quantun.architecture.usecase.product.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Gateway implementation for Product data access.
 * Bridges use case layer with persistence layer.
 */
@Component
public class ProductGatewayImpl implements ProductGateway {

    private final ProductJpaRepository repository;
    private final ProductPersistenceMapper mapper;

    public ProductGatewayImpl(ProductJpaRepository repository, ProductPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PagedResult<Product> findAll(ProductSearchCriteria criteria) {
        Specification<ProductJpaEntity> spec = Specification.allOf(
                ProductSpecifications.isActive(criteria.active()),
                ProductSpecifications.hasCategory(criteria.categoryId()),
                ProductSpecifications.nameLike(criteria.name()),
                ProductSpecifications.priceBetween(criteria.minPrice(), criteria.maxPrice()),
                ProductSpecifications.inStock(criteria.inStock())
        );

        Sort sort = Sort.by(
                criteria.sortDirection().equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                criteria.sortBy()
        );

        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        Page<ProductJpaEntity> page = repository.findAll(spec, pageable);

        List<Product> products = page.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PagedResult<>(
                products,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        return repository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toJpaEntity(product);
        ProductJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
