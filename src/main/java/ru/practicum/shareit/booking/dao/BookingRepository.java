package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 14:32
 * @project java-shareit
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking as b " +
            "where b.id=:id " +
            "and (b.booker.id=:userId or b.item.owner.id=:userId)")
    Optional<Booking> findById(@Param("id") Long bookingId, @Param("userId") Long userId);

    @Query(value = "SELECT b.start_date FROM bookings b " +
            "WHERE b.item_id = :itemId AND b.end_date > CURRENT_TIMESTAMP AND b.status = 'APPROVED' " +
            "ORDER BY b.end_date DESC " +
            "LIMIT 1", nativeQuery = true)
    Instant getLastBookingByItemId(@Param("itemId") Long itemId);

    @Query(value = "SELECT b.start_date FROM bookings b " +
            "WHERE b.item_id = :itemId AND b.start_date > CURRENT_TIMESTAMP AND b.status = 'APPROVED' " +
            "ORDER BY b.start_date ASC " +
            "LIMIT 1", nativeQuery = true)
    Instant getNextBookingByItemId(@Param("itemId") Long itemId);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus bookingStatus);

    List<Booking> findAllByBookerIdAndStatusAndStartAfterOrderByStartDesc(Long bookerId, BookingStatus status, Instant startAfter);

    List<Booking> findAllByBookerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, BookingStatus status, Instant startBefore, Instant endAfter);

    List<Booking> findAllByBookerIdAndStatusAndEndBeforeOrderByStartDesc(Long bookerId, BookingStatus status, Instant endBefore);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long itemOwnerId);

    List<Booking> findAllByItemOwnerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, BookingStatus bookingStatus, Instant startBefore, Instant endAfter);

    List<Booking> findAllByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(Long userId, BookingStatus bookingStatus, Instant endBefore);

    List<Booking> findAllByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(Long userId, BookingStatus bookingStatus, Instant startAfter);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long userId, BookingStatus bookingStatus);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, BookingStatus status, Instant endBefore);
}
