package ru.practicum.shareit.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 23:59
 * @project java-shareit
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistException extends ShareItException {
    public EmailAlreadyExistException(String email) {
        super("Email already exists: " + email);
    }
}
