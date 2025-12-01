package app.quantun.architecture.usecase.category;

import java.util.List;

/**
 * Input boundary (use case interface) for retrieving all categories.
 */
public interface GetAllCategoriesUseCase {
    List<CategoryOutputData> execute();
}
