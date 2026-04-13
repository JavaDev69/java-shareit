package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ResponseItemWithCommentDto(Long id,
                                         String name,
                                         String description,
                                         Boolean available,
                                         LocalDateTime lastBooking,
                                         LocalDateTime nextBooking,
                                         Set<ResponseCommentDto> comments) {

}
