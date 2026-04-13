package ru.practicum.shareit.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.practicum.shareit.common.exception.ShareItException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrew Vilkov
 * @created 13.04.2026 - 23:10
 * @project java-shareit
 */
class AppErrorAttributesTest {
    private AppErrorAttributes handler;

    @BeforeEach
    void setUp() {
        handler = new AppErrorAttributes();
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnFieldErrors() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");

        bindingResult.addError(new FieldError("obj", "name", "must not be blank"));
        bindingResult.addError(new ObjectError("obj", "general error"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodArgumentNotValid(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        List<String> messages = (List<String>) response.getBody().get("messages");
        assertThat(messages)
                .containsExactlyInAnyOrder(
                        "name : must not be blank",
                        "general error"
                );
    }

    @Test
    void handleHandlerMethodValidation_shouldMapParameterErrors() {
        ParameterValidationResult result = mock(ParameterValidationResult.class);
        MethodParameter methodParameter = mock(MethodParameter.class);

        when(methodParameter.getParameterName()).thenReturn("userId");
        when(result.getMethodParameter()).thenReturn(methodParameter);

        ObjectError error = new ObjectError("obj", "must be positive");
        when(result.getResolvableErrors()).thenReturn(List.of(error));

        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        when(ex.getParameterValidationResults()).thenReturn(List.of(result));

        ResponseEntity<Map<String, Object>> response =
                handler.handleHandlerMethodValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        List<String> messages = (List<String>) response.getBody().get("messages");
        assertThat(messages).containsExactly("userId : must be positive");
    }

    @Test
    void handleHandlerMethodValidation_shouldHandleBlankMessage() {
        ParameterValidationResult result = mock(ParameterValidationResult.class);
        MethodParameter methodParameter = mock(MethodParameter.class);

        when(methodParameter.getParameterName()).thenReturn("age");
        when(result.getMethodParameter()).thenReturn(methodParameter);

        ObjectError error = new ObjectError("obj", "");
        when(result.getResolvableErrors()).thenReturn(List.of(error));

        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        when(ex.getParameterValidationResults()).thenReturn(List.of(result));

        ResponseEntity<Map<String, Object>> response =
                handler.handleHandlerMethodValidation(ex);

        List<String> messages = (List<String>) response.getBody().get("messages");

        assertThat(messages)
                .containsExactly("age : Ошибка валидации параметра");
    }

    @Test
    void handleConstraintViolation_shouldMapViolations() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("must be valid");

        ConstraintViolationException ex =
                new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Map<String, Object>> response =
                handler.handleHandlerMethodValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        List<String> messages = (List<String>) response.getBody().get("messages");
        assertThat(messages).containsExactly("email : must be valid");
    }

    @Test
    void handleShareItException_shouldReturn500() {
        ShareItException ex = new ShareItException("business error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleShareItException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("messages")).isEqualTo("business error");
    }

    @Test
    void handleUnhandledException_shouldReturnGenericMessage() {
        Exception ex = new RuntimeException("boom");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnhandledException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("messages"))
                .isEqualTo("Произошла внутренняя ошибка сервера.");
    }
}