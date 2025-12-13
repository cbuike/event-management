package com.eventmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventmanagement.dto.CategoryDto;
import com.eventmanagement.dto.CreateCategoryRequest;
import com.eventmanagement.exceptions.NotFoundException;
import com.eventmanagement.exceptions.ServiceException;
import com.eventmanagement.model.Category;
import com.eventmanagement.repository.CategoryRepository;
import com.eventmanagement.service.impl.CategoryServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private final CreateCategoryRequest.CreateCategoryRequestBuilder createCategoryRequestBuilder = CreateCategoryRequest.builder();
    private final Category.CategoryBuilder categoryBuilder = Category.builder();

    @BeforeEach
    void setup() {
       createCategoryRequestBuilder
               .label("Category 1");

       categoryBuilder
               .label("Category 1")
               .parent(null)
               .children(new ArrayList<>());
    }

    @Test
    @DisplayName("Test Create Category with No Parent Id")
    void testCreateCategoryWithNoParentId_thenReturnDto()
    {
        // Arrange
        CreateCategoryRequest request = createCategoryRequestBuilder.build();
        Category category = categoryBuilder.build();

        // Act
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        CategoryDto actual = categoryService.createCategory(request, null);

        // Assert
        assertNotNull(actual);
        assertEquals(category.getId(), actual.getId());
        verify(categoryRepository).findByLabel(anyString());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Test Create Category when Label Already Exists")
    void testCreateCategoryWithExistingLabel_thenThrowServiceException() {

        // Arrange
        CreateCategoryRequest request = createCategoryRequestBuilder.build();
        Category existingCategory = categoryBuilder.build();

        // Mock findByLabel to return a category (simulating that label already exists)
        when(categoryRepository.findByLabel(request.getLabel()))
                .thenReturn(java.util.Optional.of(existingCategory));

        // Act
        ServiceException thrown = assertThrows(
                ServiceException.class,
                () -> categoryService.createCategory(request, null)
        );

        // Assert
        assertEquals("Category with the label already exists", thrown.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryRepository).findByLabel(request.getLabel());
    }

    @Test
    @DisplayName("Test Create Category with Invalid Parent Id")
    void testCreateCategoryWithInvalidParentId_thenReturnException() {

        // Arrange
        Long invalidParentId = 5L;
        CreateCategoryRequest request = createCategoryRequestBuilder
                .build();

        // Mock findById to return empty Optional, simulating parent not found
        when(categoryRepository.findById(invalidParentId))
                .thenReturn(java.util.Optional.empty());

        // Act
        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> categoryService.createCategory(request,5L)
        );

        // Assert
        assertEquals("Parent not found with id: 5", thrown.getMessage());
        verify(categoryRepository).findById(invalidParentId);
        verify(categoryRepository, never()).save(any(Category.class));
    }


    @Test
    @DisplayName("Test Get Category Subtree - Success")
    void testGetSubtreeSuccess() {
        // Arrange
        Long rootId = 1L;

        Category root = Category.builder()
                .id(1L)
                .label("Root")
                .parent(null)
                .build();

        Category child1 = Category.builder()
                .id(2L)
                .label("Child 1")
                .parent(root)
                .build();

        Category child2 = Category.builder()
                .id(3L)
                .label("Child 2")
                .parent(root)
                .build();

        // Mock repository behavior
        when(categoryRepository.findById(rootId))
                .thenReturn(Optional.of(root));

        when(categoryRepository.findSubtreeById(rootId))
                .thenReturn(List.of(root, child1, child2));

        // Act
        CategoryDto result = categoryService.getSubtree(rootId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Root", result.getLabel());
        assertEquals(2, result.getChildren().size());

        // Validate children
        List<Long> childIds = result.getChildren()
                .stream()
                .map(CategoryDto::getId)
                .toList();

        assertEquals(List.of(2L, 3L), childIds);

        // Verify mocks
        verify(categoryRepository).findById(rootId);
        verify(categoryRepository).findSubtreeById(rootId);
    }

    @Test
    @DisplayName("Get Subtree - Category Not Found")
    void testGetSubtreeNotFound() {

        // Arrange
        Long categoryId = 99L;

        // Mock findById to return empty
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());


        // Act
        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> categoryService.getSubtree(categoryId)
        );

        // Assert
        assertEquals("Category not found with id: 99", thrown.getMessage());
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never())
                .findSubtreeById(anyLong());
    }

    @Test
    @DisplayName("moveSubtree - Success")
    void testMoveSubtreeSuccess() {

        // Arrange
        Long sourceId = 1L;
        Long newParentId = 2L;

        Category source = Category.builder()
                .id(sourceId)
                .label("Source")
                .parent(null)
                .build();

        Category newParent = Category.builder()
                .id(newParentId)
                .label("New Parent")
                .parent(null)
                .build();

        // findById mocks
        when(categoryRepository.findById(sourceId))
                .thenReturn(Optional.of(source));
        when(categoryRepository.findById(newParentId))
                .thenReturn(Optional.of(newParent));

        // subtree does NOT include the new parent (valid move)
        when(categoryRepository.findSubtreeById(sourceId))
                .thenReturn(List.of(source));

        // Act
        categoryService.moveSubtree(sourceId, newParentId);

        // Assert
        assertEquals(newParent, source.getParent());
        verify(categoryRepository).findById(sourceId);
        verify(categoryRepository).findById(newParentId);
        verify(categoryRepository).findSubtreeById(sourceId);
        verify(categoryRepository).save(source);
    }

    @Test
    @DisplayName("moveSubtree - Source Not Found")
    void testMoveSubtreeSourceNotFound() {

        // Arrange
        Long sourceId = 10L;
        Long parentId = 20L;

        when(categoryRepository.findById(sourceId))
                .thenReturn(Optional.empty());

        // Act
        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> categoryService.moveSubtree(sourceId, parentId)
        );

        // Assert
        assertEquals("Source category not found with id: 10", thrown.getMessage());
        verify(categoryRepository).findById(sourceId);
        verify(categoryRepository, never()).findSubtreeById(anyLong());
        verify(categoryRepository, never()).save(any());
    }


    @Test
    @DisplayName("Delete Category - Success")
    void testDeleteCategorySuccess() {

        // Arrange
        Long categoryId = 1L;

        // Ensure the category has an ID
        Category category = Category.builder()
                .id(categoryId)
                .label("Category 1")
                .build();

        // Mock findById to return the category
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Mock findSubtreeById to return the category itself
        when(categoryRepository.findSubtreeById(categoryId)).thenReturn(List.of(category));

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).findSubtreeById(categoryId);
        verify(categoryRepository).deleteAllById(List.of(categoryId)); // Use actual ID
    }


    @Test
    @DisplayName("Delete Category - Not Found")
    void testDeleteCategoryNotFound() {

        // Arrange
        Long categoryId = 2L;

        // Mock findById to return empty (category does not exist)
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> categoryService.deleteCategory(categoryId));

        // Assert
        assertEquals("Category not found with id: 2", thrown.getMessage());
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).deleteAllById(anyList());
    }

    @Test
    @DisplayName("moveSubtree - Cannot move to itself (equal Long values)")
    void testMoveSubtreeMoveToSelf_equalLongs() {

        // Arrange
        Long sourceId = Long.valueOf(5L);
        Long newParentId = Long.valueOf(5L);

        // Act
        ServiceException thrown = assertThrows(
                ServiceException.class,
                () -> categoryService.moveSubtree(sourceId, newParentId)
        );

        // Assert
        assertEquals("Cannot move category to itself", thrown.getMessage());
        verify(categoryRepository, never()).findById(anyLong());
        verify(categoryRepository, never()).findSubtreeById(anyLong());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("moveSubtree - New parent id is null (move to root)")
    void testMoveSubtreeNewParentIsNull_movesToRoot() {

        // Arrange
        Long sourceId = 11L;
        Long newParentId = null;

        Category oldParent = Category.builder().id(99L).label("OldParent").build();
        Category source = Category.builder()
                .id(sourceId)
                .label("Source")
                .parent(oldParent) // originally had a parent
                .build();

        when(categoryRepository.findById(sourceId)).thenReturn(Optional.of(source));

        // Act
        categoryService.moveSubtree(sourceId, newParentId);

        // Assert
        assertNull(source.getParent());
        verify(categoryRepository).findById(sourceId);
        verify(categoryRepository, never()).findSubtreeById(anyLong()); // no subtree check when newParent is null
        verify(categoryRepository).save(source);
    }

    @Test
    @DisplayName("moveSubtree - Valid move when new parent exists and is NOT a descendant")
    void testMoveSubtreeValidMove_newParentNotDescendant() {

        // Arrange
        Long sourceId = 1L;
        Long newParentId = 2L;

        Category source = Category.builder()
                .id(sourceId)
                .label("Source")
                .parent(null)
                .build();

        Category newParent = Category.builder()
                .id(newParentId)
                .label("NewParent")
                .parent(null)
                .build();

        // Mocks
        when(categoryRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(categoryRepository.findById(newParentId)).thenReturn(Optional.of(newParent));

        // subtree contains only source (so newParent is not inside subtree)
        when(categoryRepository.findSubtreeById(sourceId)).thenReturn(List.of(source));

        // Act
        categoryService.moveSubtree(sourceId, newParentId);

        // Assert
        assertEquals(newParent, source.getParent());
        verify(categoryRepository).findById(sourceId);
        verify(categoryRepository).findById(newParentId);
        verify(categoryRepository).findSubtreeById(sourceId);
        verify(categoryRepository).save(source);
    }

    @Test
    @DisplayName("moveSubtree - New parent is a descendant -> ServiceException")
    void testMoveSubtreeNewParentIsDescendant_explicit() {

        // Arrange
        Long sourceId = 1L;
        Long newParentId = 3L;

        Category source = Category.builder()
                .id(sourceId)
                .label("Source")
                .build();

        // newParent is a child (descendant) of source
        Category descendant = Category.builder()
                .id(newParentId)
                .label("Descendant")
                .parent(source)
                .build();

        // Mocks
        when(categoryRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(categoryRepository.findById(newParentId)).thenReturn(Optional.of(descendant));

        // subtree contains the descendant -> should trigger the check
        when(categoryRepository.findSubtreeById(sourceId)).thenReturn(List.of(source, descendant));

        // Act
        ServiceException thrown = assertThrows(
                ServiceException.class,
                () -> categoryService.moveSubtree(sourceId, newParentId)
        );

        // Assert
        assertEquals("New parent cannot be a descendant of the source category", thrown.getMessage());
        verify(categoryRepository).findById(sourceId);
        verify(categoryRepository).findById(newParentId);
        verify(categoryRepository).findSubtreeById(sourceId);
        verify(categoryRepository, never()).save(any());
    }
}
