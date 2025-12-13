package com.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CreateCategoryRequest {
    @Schema(description = "Label of the category", required = true, example = "Sports")
    private String label;
}
