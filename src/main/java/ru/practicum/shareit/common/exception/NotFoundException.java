package ru.practicum.shareit.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 23:52
 * @project java-shareit
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends ShareItException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, String... params) {
        this(String.format(message, params));
    }
}
