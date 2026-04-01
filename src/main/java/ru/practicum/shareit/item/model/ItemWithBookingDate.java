package ru.practicum.shareit.item.model;

import java.time.Instant;

/**
 * @author Andrew Vilkov
 * @created 26.03.2026 - 21:46
 * @project java-shareit
 */
public interface ItemWithBookingDate {
    Long getId();

    String getName();

    String getDescription();

    Boolean getIsAvailable();

    Instant getLastBooking();

    Instant getNextBooking();
}
