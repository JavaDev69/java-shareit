package ru.practicum.shareit.booking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Andrew Vilkov
 * @created 24.03.2026 - 20:21
 * @project java-shareit
 */
@Documented
@Constraint(validatedBy = StartBeforeEndValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StartBeforeEnd {
    String message() default "Дата начала бронирования должна быть раньше даты окончания бронирования.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
