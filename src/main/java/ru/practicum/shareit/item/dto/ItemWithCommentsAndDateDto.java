package ru.practicum.shareit.item.dto;

import lombok.Builder;
import ru.practicum.shareit.item.model.ItemComment;

import java.time.Instant;
import java.util.Set;

@Builder(toBuilder = true)
public record ItemWithCommentsAndDateDto(Long id,
                                         String name,
                                         String description,
                                         Boolean available,
                                         Instant lastBooking,
                                         Instant nextBooking,
                                         Set<ItemComment> comments) {

}
