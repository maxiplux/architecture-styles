package app.quantun.architecture.usecase.gateway;

import app.quantun.architecture.entity.Product;
import app.quantun.architecture.usecase.product.ProductSearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Gateway interface for Product data access operations.
 * Defined in the use case layer, implemented in the interface adapter layer.
 */
public interface ProductGateway {
    PagedResult<Product> findAll(ProductSearchCriteria criteria);
    Optional<Product> findById(Long id);
    List<Product> findAllByIds(List<Long> ids);
    Product save(Product product);
}
