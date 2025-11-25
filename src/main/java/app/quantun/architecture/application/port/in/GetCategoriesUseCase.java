package app.quantun.architecture.application.port.in;

import app.quantun.architecture.domain.model.Category;
import java.util.List;

public interface GetCategoriesUseCase {
    List<Category> getAllActiveCategories();
}