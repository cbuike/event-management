package com.eventmanagement.service;

import com.eventmanagement.dto.CategoryDto;
import com.eventmanagement.dto.CreateCategoryRequest;

/**
 * Service interface for managing catgeories.
 * This service provides business logic for managing categories and their subtrees
 * associated with patients.
 *
 * @author ChibuikeOkeke
 * @version 1.0
 * @since 1.0
 */
public interface CategoryService {

    CategoryDto createCategory(CreateCategoryRequest request, Long parentId);
    CategoryDto getSubtree(Long id);
    void deleteCategory(Long id);
    void moveSubtree(Long sourceId, Long newParentId);
}
