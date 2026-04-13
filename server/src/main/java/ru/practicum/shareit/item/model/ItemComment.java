package ru.practicum.shareit.item.model;

import java.time.Instant;

/**
 * @author Andrew Vilkov
 * @created 28.03.2026 - 11:37
 * @project java-shareit
 */
public record ItemComment(
        Long id,
        Long itemId,
        String text,
        String authorName,
        Instant created
) {

/*    Long getId();

    Long getItemId();

    String getText();

    String getAuthorName();

    Instant getCreated();*/
}
