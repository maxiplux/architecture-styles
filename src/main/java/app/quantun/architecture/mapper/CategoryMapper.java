package app.quantun.architecture.mapper;

import app.quantun.architecture.domain.Category;
import app.quantun.architecture.dto.CategoryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDto(Category category);
}
