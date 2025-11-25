package app.quantun.architecture.adapter.in.web.mapper;

import app.quantun.architecture.adapter.in.web.dto.CategoryResponse;
import app.quantun.architecture.domain.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryWebMapper {

    CategoryResponse toResponse(Category category);
}