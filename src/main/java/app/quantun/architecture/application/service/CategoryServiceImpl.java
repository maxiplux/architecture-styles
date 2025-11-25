package app.quantun.architecture.application.service;

import app.quantun.architecture.application.port.in.GetCategoriesUseCase;
import app.quantun.architecture.application.port.out.CategoryRepositoryPort;
import app.quantun.architecture.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements GetCategoriesUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllActive();
    }
}