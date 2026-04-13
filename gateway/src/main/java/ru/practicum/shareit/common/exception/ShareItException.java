package ru.practicum.shareit.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Общее исключение для все исключений приложения.
 *
 * @author Andrew Vilkov
 * @created 08.03.2026 - 23:53
 * @project java-shareit
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ShareItException extends RuntimeException {
    public ShareItException(String message) {
        super(message);
    }
}
