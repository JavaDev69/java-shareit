package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

/**
 * @author Andrew Vilkov
 * @created 27.03.2026 - 14:38
 * @project java-shareit
 */
public record ResponseCommentDto(
        Long id,
        String text,
        String authorName,
        LocalDateTime created) {
}
