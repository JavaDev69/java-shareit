package ru.practicum.shareit.common;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import ru.practicum.shareit.common.exception.ShareItException;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 14:35
 * @project java-shareit
 */
@Log4j2
@Component
public class AppErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        Throwable error = getError(webRequest);
        log.error("Произошла ошибка", error);
        if (error instanceof MethodArgumentNotValidException er) {
            List<String> message = er.getAllErrors().stream()
                    .map(e -> {
                        if (e instanceof FieldError fe) {
                            return String.join(" : ", fe.getField(), fe.getDefaultMessage());
                        } else {
                            return e.getDefaultMessage();
                        }
                    })
                    .toList();
            errorAttributes.put("messages", message);
        } else if (error instanceof ShareItException) {
            errorAttributes.put("messages", error.getMessage());
        }
        return errorAttributes;
    }
}
