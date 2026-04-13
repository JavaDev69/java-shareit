package ru.practicum.shareit.common;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import ru.practicum.shareit.common.exception.NotFoundException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AppErrorAttributesTest {
    private final AppErrorAttributes errorAttributes = new AppErrorAttributes();

    @Test
    void getErrorAttributesShouldAddFieldAndGlobalValidationMessages() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        bindingResult.addError(new ObjectError("request", "global validation failed"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);
        ServletWebRequest webRequest = createWebRequest(exception);

        Map<String, Object> actual =
                errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        assertThat(actual).containsEntry("messages", List.of("email : must not be blank", "global validation failed"));
    }

    @Test
    void getErrorAttributesShouldAddMessageForShareItException() {
        NotFoundException exception = new NotFoundException("Entity not found");
        ServletWebRequest webRequest = createWebRequest(exception);

        Map<String, Object> actual =
                errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        assertThat(actual).containsEntry("messages", "Entity not found");
    }

    @Test
    void getErrorAttributesShouldNotAddMessagesForNonShareItException() {
        RuntimeException exception = new RuntimeException("Unexpected error");
        ServletWebRequest webRequest = createWebRequest(exception);

        Map<String, Object> actual =
                errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        assertThat(actual).doesNotContainKey("messages");
    }

    private ServletWebRequest createWebRequest(Throwable throwable) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);
        webRequest.setAttribute(
                RequestDispatcher.ERROR_EXCEPTION,
                throwable,
                RequestAttributes.SCOPE_REQUEST
        );
        return webRequest;
    }
}
