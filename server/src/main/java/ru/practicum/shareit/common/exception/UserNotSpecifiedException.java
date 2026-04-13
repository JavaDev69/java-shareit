package ru.practicum.shareit.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 13:30
 * @project java-shareit
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserNotSpecifiedException extends ShareItException {
    public UserNotSpecifiedException() {
        super("Пользователь не задан.");
    }
}
