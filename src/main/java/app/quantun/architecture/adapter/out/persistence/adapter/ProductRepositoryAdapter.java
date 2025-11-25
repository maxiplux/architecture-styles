package app.quantun.architecture.adapter.out.persistence.adapter;

import app.quantun.architecture.adapter.out.persistence.mapper.ProductPersistenceMapper;
import app.quantun.architecture.adapter.out.persistence.repository.ProductJpaRepository;
import app.quantun.architecture.adapter.out.persistence.specification.ProductJpaSpecifications;
import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import app.quantun.architecture.application.port.out.ProductRepositoryPort;
import app.quantun.architecture.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;
    private final ProductPersistenceMapper mapper;

    @Override
    public Page<Product> findAll(ProductSearchCriteria criteria, int page, int size,
                                  String sortBy, String sortDirection) {
        var spec = ProductJpaSpecifications.fromCriteria(criteria);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var entityPage = jpaRepository.findAll(spec, pageable);

        return entityPage.map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Product save(Product product) {
        var entity = mapper.toEntity(product);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}