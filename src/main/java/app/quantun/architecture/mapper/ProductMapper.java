package app.quantun.architecture.mapper;

import app.quantun.architecture.domain.Product;
import app.quantun.architecture.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDTO toDto(Product product);
}
