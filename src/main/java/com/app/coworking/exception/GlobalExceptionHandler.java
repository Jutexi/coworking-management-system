package com.app.coworking.exception;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler { // может все же убрать отсюда логгер

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Ошибки Bean Validation (@NotBlank, @Email, @Future, @Size и т.д.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        logger.error("Validation failed: {}", errors, ex);

        return ResponseEntity.badRequest().body(errors);
    }

    // Ошибки при десериализации JSON (например, неправильный формат даты)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormatException(
            HttpMessageNotReadableException ex) {
        String message = "Invalid request format: " + ex.getMostSpecificCause().getMessage();

        logger.error("JSON deserialization error: {}", message, ex);

        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    // ResourceNotFoundException → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            ResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    // AlreadyExistsException
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyExists(AlreadyExistsException ex) {
        logger.error("Already exists error: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    // Новый обработчик для InvalidArgumentException
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidArgument(InvalidArgumentException ex) {
        logger.error("Invalid argument error: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    // Общий fallback на все другие исключения
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected error occurred: " + ex.getMessage()));
    }
}



