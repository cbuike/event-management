package com.eventmanagement.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleServiceFoundException_ShouldReturnConflictResponse() {
        ServiceException ex = new ServiceException("Service entity exists");

        ResponseEntity<Object> response = exceptionHandler.handleServiceFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getError()).isEqualTo("Entity Already Exists");
        assertThat(body.getMessage()).isEqualTo("Service entity exists");
    }

    @Test
    void handleNotFoundException_ShouldReturnNotFoundResponse() {
        NotFoundException ex = new NotFoundException("Entity not found");

        ResponseEntity<Object> response = exceptionHandler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Entity Not Found");
        assertThat(body.getMessage()).isEqualTo("Entity not found");
    }

    @Test
    void handleRuntimeException_ShouldReturnInternalServerErrorResponse() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<Object> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getError()).isEqualTo("Internal Server Error");
        assertThat(body.getMessage()).isEqualTo("Something went wrong");
    }

    @SuppressWarnings("unchecked")
    @Test
    void handleValidation_ShouldReturnBadRequestResponse() {
        // Mock a FieldError
        FieldError fieldError = new FieldError("objectName", "field1", "must not be null");
        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(new Object(), "objectName");
        bindingResult.addError(fieldError);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Object> response = exceptionHandler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Validation Error");
        assertThat(body.get("message")).isEqualTo("Invalid request data");

        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertThat(errors).containsEntry("field1", "must not be null");
    }
}
