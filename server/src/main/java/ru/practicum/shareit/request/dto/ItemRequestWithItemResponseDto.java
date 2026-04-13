package ru.practicum.shareit.request.dto;

import lombok.Builder;
import ru.practicum.shareit.item.model.ItemForRequest;

import java.time.Instant;
import java.util.List;

@Builder(toBuilder = true)
public record ItemRequestWithItemResponseDto(
        Long id,
        String description,
        Instant created,
        List<ItemForRequest> items) {
}
