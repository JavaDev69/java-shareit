package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.ItemNotAvailableException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * @author Andrew Vilkov
 * @created 18.03.2026 - 20:16
 * @project java-shareit
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Booking save(Booking booking) {
        Long itemId = booking.getItem().getId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=%d not found", itemId));
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException();
        }
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking setApproveStatus(Long userId, Long bookingId, boolean approved) {
        Booking booking = findBookingOrThrow(bookingId);
        validateBookingStatus(booking);
        validateUserIsOwner(userId, booking);
        Item item = booking.getItem();

        validateItemAvailability(item);

        if (approved) {
            item.setAvailable(false);
            itemRepository.save(item);
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking findById(long bookingId, long userId) {
        checkUserExist(userId);
        return bookingRepository.findById(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Booking with id=%d not found", bookingId));
    }

    @Override
    public List<Booking> findByUserIdAndState(Long userId, BookingState bookingState) {
        checkUserExist(userId);
        return switch (bookingState) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository
                    .findAllByBookerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(userId, BookingStatus.APPROVED, Instant.now(), Instant.now());
            case PAST -> bookingRepository
                    .findAllByBookerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, Instant.now());
            case FUTURE -> bookingRepository
                    .findAllByBookerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, Instant.now());
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
    }

    @Override
    public List<Booking> findByOwnerIdAndState(Long userId, BookingState bookingState) {
        checkUserExist(userId);
        return switch (bookingState) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository
                    .findAllByItemOwnerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(userId, BookingStatus.APPROVED, Instant.now(), Instant.now());
            case PAST -> bookingRepository
                    .findAllByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(userId, BookingStatus.APPROVED, Instant.now());
            case FUTURE -> bookingRepository
                    .findAllByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(userId, BookingStatus.APPROVED, Instant.now());
            case WAITING ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
    }

    private Booking findBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id=%d not found", bookingId));
    }

    private void checkUserExist(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id=%d not found", userId));
    }

    private void validateBookingStatus(Booking booking) {
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ForbiddenException("Вещь не находится в статусе ожидания одобрения.");
        }
    }

    private void validateItemAvailability(Item item) {
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException();
        }
    }

    private void validateUserIsOwner(Long userId, Booking booking) {
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new ForbiddenException("Пользователь не является владельцем вещи.");
        }
    }
}
