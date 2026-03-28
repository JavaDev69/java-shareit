package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 18.03.2026 - 20:15
 * @project java-shareit
 */
public interface BookingService {
    Booking save(Booking booking);

    Booking findById(long bookingId, long userId);

    List<Booking> findByUserIdAndState(Long userId, BookingState bookingState);

    List<Booking> findByOwnerIdAndState(Long userId, BookingState bookingState);

    Booking setApproveStatus(Long userId, Long bookingId, boolean approved);
}
