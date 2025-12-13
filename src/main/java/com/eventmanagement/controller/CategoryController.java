package com.eventmanagement.controller;

import com.eventmanagement.dto.CategoryDto;
import com.eventmanagement.dto.CreateCategoryRequest;
import com.eventmanagement.dto.MoveRequest;
import com.eventmanagement.exceptions.ErrorResponse;
import com.eventmanagement.exceptions.NotFoundException;
import com.eventmanagement.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing event categories.
 * <p>
 * Provides endpoints for creating, retrieving, moving, and deleting
 * hierarchical event categories.
 *
 * @author Chibuike Okeke
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Category", description = "Category management APIs")
@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category.
     *
     * @param request the category creation request
     * @param parentId the If parent category (it's optional)
     * @return the created category
     * @throws NotFoundException if a parentId is provided but the parent category does not exist
     */
    @Operation(
            summary = "Create a category",
            description = "Creates a new category. Optionally accepts a parent category."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Category successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parent category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category with the given label already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            @RequestParam(name = "parentId", required = false) Long parentId
    ) {
        return ResponseEntity.status(201)
                .body(categoryService.createCategory(request, parentId));
    }

    /**
     * Retrieve the subtree of a category.
     *
     * @param parentId the parent category ID
     * @return the category subtree
     * @throws NotFoundException if the category does not exist
     */
    @Operation(
            summary = "Get category subtree",
            description = "Retrieves a category and all its nested children"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category subtree retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{parentId}/subtree")
    public ResponseEntity<CategoryDto> getSubtree(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getSubtree(parentId));
    }

    /**
     * Move a category subtree to a new parent.
     *
     * @param subtreeId the root category of the subtree to move
     * @param request   the move request containing the new parent ID
     * @return empty response with HTTP 200 status
     */
    @Operation(
            summary = "Move a category subtree",
            description = "Moves a category and its children to another parent category"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category subtree moved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category or new parent not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping("/{subtreeId}/move")
    public ResponseEntity<Void> moveSubtree(
            @PathVariable Long subtreeId,
            @Valid @RequestBody MoveRequest request
    ) {
        categoryService.moveSubtree(subtreeId, request.newParentId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a category.
     *
     * @param id the category ID
     * @return empty response with HTTP 204 status
     * @throws NotFoundException if the category does not exist
     */
    @Operation(
            summary = "Delete a category",
            description = "Deletes a category by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Category deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
