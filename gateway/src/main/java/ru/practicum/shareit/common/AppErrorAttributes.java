package ru.practicum.shareit.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.practicum.shareit.common.exception.ShareItException;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 14:35
 * @project java-shareit
 */
@Slf4j
@RestControllerAdvice
public class AppErrorAttributes {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Ошибка валидации тела запроса: {}", ex.getBody());
        List<String> messages = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + " : " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .toList();

        return ResponseEntity.badRequest().body(Map.of("messages", messages));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        log.error("Ошибка валидации параметров запроса", ex);
        List<String> messages = ex.getParameterValidationResults().stream()
                .flatMap(result -> mapParameterValidationResult(result).stream())
                .toList();

        return ResponseEntity.badRequest().body(Map.of("messages", messages));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidation(ConstraintViolationException ex) {
        log.error("Ошибка валидации параметров запроса", ex);
        List<String> messages = ex.getConstraintViolations().stream()
                .map(this::mapParameterValidationResult)
                .toList();

        return ResponseEntity.badRequest().body(Map.of("messages", messages));
    }

    @ExceptionHandler(ShareItException.class)
    public ResponseEntity<Map<String, Object>> handleShareItException(ShareItException ex) {
        log.error("Ошибка приложения", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("messages", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnhandledException(Exception ex) {
        log.error("Необработанная ошибка", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("messages", "Произошла внутренняя ошибка сервера."));
    }

    private List<String> mapParameterValidationResult(ParameterValidationResult result) {
        return result.getResolvableErrors().stream()
                .map(error -> {
                    String message = error.getDefaultMessage();
                    if (message == null || message.isBlank()) {
                        message = "Ошибка валидации параметра";
                    }
                    return result.getMethodParameter().getParameterName() + " : " + message;
                })
                .toList();
    }

    private String mapParameterValidationResult(ConstraintViolation<?> result) {
        return result.getPropertyPath() + " : " + result.getMessage();
    }
}
