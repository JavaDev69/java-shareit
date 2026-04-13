package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.common.RequestHeaders;

import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

/**
 * @author Andrew Vilkov
 * @created 07.04.2026 - 21:06
 * @project java-shareit
 */
@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(RequestHeaders.USER_ID_HEADER) long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") BookingState state,
                                              @Valid @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Valid @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Fetching bookings for user {} with state {}, from {}, size {}", userId, state, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnBookings(@RequestHeader(RequestHeaders.USER_ID_HEADER) long userId,
                                                 @RequestParam(name = "state", defaultValue = "ALL") BookingState state,
                                                 @Valid @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @Valid @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Fetching own bookings for user {} with state {}, from {}, size {}", userId, state, from, size);
        return bookingClient.getOwnBookings(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(RequestHeaders.USER_ID_HEADER) long userId,
                                           @RequestBody @Valid BookingRequestDto requestDto) {
        log.info("Creating booking {} for user {}", requestDto, userId);
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(RequestHeaders.USER_ID_HEADER) long userId,
                                             @Valid @PositiveOrZero @PathVariable Long bookingId) {
        log.info("Fetching booking {} for user {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @PositiveOrZero @PathVariable Long bookingId,
                                         @RequestParam("approved") boolean approved) {
        log.info("Updating booking {} approved {} by user {}", bookingId, approved, userId);
        return bookingClient.updateBooking(userId, bookingId, approved);

    }
}
