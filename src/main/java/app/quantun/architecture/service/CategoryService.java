package app.quantun.architecture.service;

import app.quantun.architecture.domain.Category;
import app.quantun.architecture.dto.CategoryDTO;
import app.quantun.architecture.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> findAll() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .toList();
    }

    private CategoryDTO toDto(Category c) {
        return new CategoryDTO(c.getId(), c.getName(), c.getDescription(), c.isActive(), c.getCreatedAt());
    }
}
