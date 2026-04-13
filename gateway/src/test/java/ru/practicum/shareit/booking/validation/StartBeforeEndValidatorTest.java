package ru.practicum.shareit.booking.validation;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StartBeforeEndValidatorTest {

    private final StartBeforeEndValidator validator = new StartBeforeEndValidator();

    @Test
    void isValidShouldReturnTrueWhenDtoNull() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void isValidShouldReturnTrueWhenStartOrEndIsNull() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setStart(LocalDateTime.now().plusHours(1));

        assertThat(validator.isValid(dto, null)).isTrue();
    }

    @Test
    void isValidShouldReturnTrueWhenStartBeforeEnd() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThat(validator.isValid(dto, null)).isTrue();
    }

    @Test
    void isValidShouldReturnFalseWhenStartAfterEnd() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setStart(LocalDateTime.now().plusHours(2));
        dto.setEnd(LocalDateTime.now().plusHours(1));

        assertThat(validator.isValid(dto, null)).isFalse();
    }
}
