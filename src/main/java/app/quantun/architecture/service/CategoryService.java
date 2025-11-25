package app.quantun.architecture.service;

import app.quantun.architecture.domain.Category;
import app.quantun.architecture.dto.CategoryDTO;
import app.quantun.architecture.mapper.CategoryMapper;
import app.quantun.architecture.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDTO> findAll() {
        return categoryRepository.findByActiveTrue().stream()
                .map(categoryMapper::toDto)
                .toList();
    }
}
