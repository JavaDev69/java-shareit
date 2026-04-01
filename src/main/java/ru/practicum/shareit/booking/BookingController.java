package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper mapper;

    @PostMapping
    public ResponseBookingDto save(@RequestHeader(USER_ID_HEADER) Long userId,
                                   @RequestBody @Valid BookingDto booking) {
        booking.setBookerId(userId);
        Booking entity = mapper.toEntity(booking);
        Booking saved = bookingService.save(entity);
        return mapper.toDto(saved);
    }

    @PatchMapping("/{bookingId}")
    public ResponseBookingDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                                     @PathVariable Long bookingId,
                                     @RequestParam("approved") boolean approved) {

        Booking update = bookingService.setApproveStatus(userId, bookingId, approved);
        return mapper.toDto(update);
    }

    @GetMapping("/{bookingId}")
    public ResponseBookingDto findOne(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long bookingId) {
        Booking byId = bookingService.findById(bookingId, userId);
        return mapper.toDto(byId);
    }

    @GetMapping
    public List<ResponseBookingDto> findAllForBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                     @RequestParam(value = "state") Optional<BookingState> state) {
        return bookingService.findByUserIdAndState(userId, state.orElse(BookingState.ALL))
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/owner")
    public List<ResponseBookingDto> findAllForOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                    @RequestParam(value = "state") Optional<BookingState> state) {
        return bookingService.findByOwnerIdAndState(userId, state.orElse(BookingState.ALL))
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
