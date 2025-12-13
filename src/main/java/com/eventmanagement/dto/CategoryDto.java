package com.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {

    @Schema(description = "Id of the category", example = "3")
    private Long id;

    @Schema(description = "Label of the category", example = "Sports")
    private String label;

    @Schema(description = "Label of the category", example = "1")
    private Long parentId;

    @Schema(description = "Category Subtree")
    public List<CategoryDto> children;
}
