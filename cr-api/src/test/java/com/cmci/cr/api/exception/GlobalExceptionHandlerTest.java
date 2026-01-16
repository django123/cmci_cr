package com.cmci.cr.api.exception;

import com.cmci.cr.api.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour GlobalExceptionHandler
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/cr");
    }

    @Test
    @DisplayName("Devrait gérer IllegalArgumentException avec BAD_REQUEST")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid data");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid data");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Devrait gérer IllegalStateException avec CONFLICT")
    void shouldHandleIllegalStateException() {
        // Given
        IllegalStateException exception = new IllegalStateException("Invalid state");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalStateException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid state");
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("Devrait gérer NoSuchElementException avec NOT_FOUND")
    void shouldHandleNoSuchElementException() {
        // Given
        NoSuchElementException exception = new NoSuchElementException("Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNoSuchElementException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("Devrait gérer AccessDeniedException avec FORBIDDEN")
    void shouldHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Devrait gérer Exception générique avec INTERNAL_SERVER_ERROR")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }
}
