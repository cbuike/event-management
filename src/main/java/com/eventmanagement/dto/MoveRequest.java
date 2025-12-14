package com.eventmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MoveRequest {
    @Schema(example = "3")
    public Long newParentId;
}
