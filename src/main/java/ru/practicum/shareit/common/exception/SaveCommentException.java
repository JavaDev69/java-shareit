package ru.practicum.shareit.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 23:52
 * @project java-shareit
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SaveCommentException extends ShareItException {
    public SaveCommentException(String message) {
        super(message);
    }

    public SaveCommentException(String message, String... params) {
        this(String.format(message, params));
    }
}
