package ru.practicum.shareit.common;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 13.04.2026 - 23:32
 * @project java-shareit
 */
public record ErrorResponseBody(int error, List<String> messages) {
}
