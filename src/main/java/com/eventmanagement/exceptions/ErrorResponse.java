package com.eventmanagement.exceptions;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private String error;
    private Integer status;
    private String message;
}
