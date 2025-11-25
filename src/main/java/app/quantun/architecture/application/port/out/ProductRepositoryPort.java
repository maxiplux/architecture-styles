package app.quantun.architecture.application.port.out;

import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import app.quantun.architecture.domain.model.Product;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Page<Product> findAll(ProductSearchCriteria criteria, int page, int size, String sortBy, String sortDirection);
    Optional<Product> findById(Long id);
    List<Product> findAllByIds(List<Long> ids);
    Product save(Product product);
}