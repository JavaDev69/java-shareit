package ru.practicum.shareit.request.dto;

import lombok.Builder;

import java.time.Instant;

@Builder(toBuilder = true)
public record ItemRequestResponseDto(Long id, String description, Instant created) {
}
