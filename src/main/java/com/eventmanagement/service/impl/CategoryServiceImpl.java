package com.eventmanagement.service.impl;

import com.eventmanagement.dto.CategoryDto;
import com.eventmanagement.dto.CreateCategoryRequest;
import com.eventmanagement.exceptions.NotFoundException;
import com.eventmanagement.exceptions.ServiceException;
import com.eventmanagement.model.Category;
import com.eventmanagement.repository.CategoryRepository;
import com.eventmanagement.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link CategoryService} interface.
 * Provides core operations for category management,
 * including creation, deletion, subtree retrieval, and moving subtrees.
 * Handles category data persistence and business logic.
 *
 * @author Chibuike Okeke
 * @version 1.0
 * @since 1.0
 */
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public CategoryDto createCategory(CreateCategoryRequest request, Long parentId) {
        String label = request.getLabel();

        // Fetch parent if provided
        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent not found with id: " + parentId));
        }

        // Check for duplicate label
        if (categoryRepository.findByLabel(label).isPresent()) {
            throw new ServiceException("Category with the label already exists");
        }

        Category category = Category.builder()
                .label(label)
                .parent(parent)
                .build();

        return toDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getSubtree(Long id) {
        // Ensure category exists
        categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        List<Category> categories = categoryRepository.findSubtreeById(id);
        Map<Long, CategoryDto> dtoMap = new HashMap<>();

        // Convert all categories to DTOs
        for (Category category : categories) {
            dtoMap.put(category.getId(), CategoryDto.builder()
                    .id(category.getId())
                    .label(category.getLabel())
                    .parentId(category.getParent() != null ? category.getParent().getId() : null)
                    .children(new ArrayList<>())
                    .build());
        }

        // Build parent-child relationships
        for (Category category : categories) {
            if (category.getParent() != null) {
                CategoryDto parentDto = dtoMap.get(category.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dtoMap.get(category.getId()));
                }
            }
        }

        return dtoMap.get(id);
    }

    @Transactional
    @Override
    public void deleteCategory(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        List<Long> idsToDelete = categoryRepository.findSubtreeById(id)
                .stream()
                .map(Category::getId)
                .toList();

        categoryRepository.deleteAllById(idsToDelete);
    }

    @Transactional
    @Override
    public void moveSubtree(Long sourceId, Long newParentId) {
        if (Objects.equals(sourceId, newParentId)) {
            throw new ServiceException("Cannot move category to itself");
        }

        Category source = categoryRepository.findById(sourceId)
                .orElseThrow(() -> new NotFoundException("Source category not found with id: " + sourceId));

        Category newParent = null;
        if (newParentId != null) {
            newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new NotFoundException("New parent category not found with id: " + newParentId));

            // Prevent moving into its own subtree
            Set<Long> subtreeIds = categoryRepository.findSubtreeById(sourceId)
                    .stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet());
            if (subtreeIds.contains(newParent.getId())) {
                throw new ServiceException("New parent cannot be a descendant of the source category");
            }
        }

        source.setParent(newParent);
        categoryRepository.save(source);
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .label(category.getLabel())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(category.getChildren() != null
                        ? category.getChildren().stream().map(this::toDto).toList()
                        : Collections.emptyList())
                .build();
    }
}
