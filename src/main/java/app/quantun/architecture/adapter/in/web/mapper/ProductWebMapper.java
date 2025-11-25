package app.quantun.architecture.adapter.in.web.mapper;

import app.quantun.architecture.adapter.in.web.dto.ProductFilterRequest;
import app.quantun.architecture.adapter.in.web.dto.ProductResponse;
import app.quantun.architecture.application.port.in.ProductSearchCriteria;
import app.quantun.architecture.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponse(Product product);

    ProductSearchCriteria toCriteria(ProductFilterRequest filter);
}