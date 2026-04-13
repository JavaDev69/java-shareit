package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import ru.practicum.shareit.booking.validation.StartBeforeEnd;

import java.time.LocalDateTime;

@Data
@StartBeforeEnd
public class BookingRequestDto {
    Long bookerId;
    @NotNull(message = "Обязательное поле")
    @PositiveOrZero(message = "Id должно быть положительным числом или 0")
    private Long itemId;
    @NotNull(message = "Обязательное поле")
    @FutureOrPresent(message = "Дата должна быть не реньше текущей даты.")
    private LocalDateTime start;
    @NotNull(message = "Обязательное поле")
    @Future(message = "Дата должна быть в будущем.")
    private LocalDateTime end;
}