package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.user.dto.UserIdDto;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record ResponseBookingDto(
        Long id,
        BookingStatus status,
        UserIdDto booker,
        ResponseItemDto item,
        LocalDateTime start,
        LocalDateTime end) {
}