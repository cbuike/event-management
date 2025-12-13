package com.eventmanagement.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MoveRequest {
    public Long newParentId;
}
