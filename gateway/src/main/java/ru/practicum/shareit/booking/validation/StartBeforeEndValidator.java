package ru.practicum.shareit.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

/**
 * @author Andrew Vilkov
 * @created 24.03.2026 - 20:22
 * @project java-shareit
 */
public class StartBeforeEndValidator implements ConstraintValidator<StartBeforeEnd, BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getStart() == null || dto.getEnd() == null) {
            return true;
        }

        return dto.getStart().isBefore(dto.getEnd());
    }
}
