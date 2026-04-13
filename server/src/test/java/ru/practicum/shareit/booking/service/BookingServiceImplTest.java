package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Andrew Vilkov
 * @created 12.04.2026 - 09:59
 * @project java-shareit
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("owner").email("owner@test.local").build();
        booker = User.builder().id(2L).name("booker").email("booker@test.local").build();
        item = Item.builder()
                .id(10L)
                .name("item")
                .description("desc")
                .available(true)
                .owner(owner)
                .build();
        booking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(Instant.now().plusSeconds(3_600))
                .end(Instant.now().plusSeconds(7_200))
                .build();
    }

    @Test
    void saveShouldPersistBookingWhenItemExistsAndAvailable() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking actual = bookingService.save(booking);

        assertSame(booking, actual);
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).save(booking);
    }

    @Test
    void saveShouldThrowWhenItemNotFound() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> bookingService.save(booking));

        assertEquals("Item with id=10 not found", ex.getMessage());
        verify(itemRepository).findById(item.getId());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void saveShouldThrowWhenItemIsNotAvailable() {
        item.setAvailable(false);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemNotAvailableException ex = assertThrows(
                ItemNotAvailableException.class,
                () -> bookingService.save(booking)
        );

        assertEquals("Вещь недоступна для бронирования.", ex.getMessage());
        verify(itemRepository).findById(item.getId());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void setApproveStatusShouldApproveWhenOwnerAndWaitingAndItemAvailable() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(itemRepository.save(item)).thenReturn(item);
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking actual = bookingService.setApproveStatus(owner.getId(), booking.getId(), true);

        assertSame(booking, actual);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).findById(booking.getId());
        verify(itemRepository).save(item);
        verify(bookingRepository).save(booking);
    }

    @Test
    void setApproveStatusShouldRejectWhenOwnerAndWaitingAndItemAvailable() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking actual = bookingService.setApproveStatus(owner.getId(), booking.getId(), false);

        assertSame(booking, actual);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
        verify(bookingRepository).findById(booking.getId());
        verify(itemRepository, never()).save(any(Item.class));
        verify(bookingRepository).save(booking);
    }

    @Test
    void setApproveStatusShouldThrowWhenBookingNotFound() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.setApproveStatus(owner.getId(), booking.getId(), true));

        assertEquals("Booking with id=100 not found", ex.getMessage());
        verify(bookingRepository).findById(booking.getId());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void setApproveStatusShouldThrowWhenBookingStatusNotWaiting() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> bookingService.setApproveStatus(owner.getId(), booking.getId(), true));

        assertEquals(
                "Вещь не находится в статусе ожидания одобрения.",
                ex.getMessage()
        );
        verify(bookingRepository).findById(booking.getId());
        verifyNoInteractions(itemRepository);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void setApproveStatusShouldThrowWhenUserIsNotOwner() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> bookingService.setApproveStatus(booker.getId(), booking.getId(), true));

        assertEquals("Пользователь не является владельцем вещи.", ex.getMessage());
        verify(bookingRepository).findById(booking.getId());
        verifyNoInteractions(itemRepository);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void setApproveStatusShouldThrowWhenItemIsNotAvailable() {
        item.setAvailable(false);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ItemNotAvailableException ex = assertThrows(ItemNotAvailableException.class,
                () -> bookingService.setApproveStatus(owner.getId(), booking.getId(), true));

        assertEquals("Вещь недоступна для бронирования.", ex.getMessage());
        verify(bookingRepository).findById(booking.getId());
        verify(itemRepository, never()).save(any(Item.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void findByIdShouldReturnBookingWhenUserExistsAndHasAccess() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId(), owner.getId())).thenReturn(Optional.of(booking));

        Booking actual = bookingService.findById(booking.getId(), owner.getId());

        assertSame(booking, actual);
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findById(booking.getId(), owner.getId());
    }

    @Test
    void findByIdShouldThrowWhenUserNotFound() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.findById(booking.getId(), owner.getId()));

        assertEquals("User with id=1 not found", ex.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository, never()).findById(anyLong(), anyLong());
    }

    @Test
    void findByIdShouldThrowWhenBookingNotFoundForUser() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId(), owner.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.findById(booking.getId(), owner.getId()));

        assertEquals("Booking with id=100 not found", ex.getMessage());
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findById(booking.getId(), owner.getId());
    }

    @Test
    void findByUserIdAndStateShouldReturnAllBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(booker.getId())).thenReturn(expected);

        List<Booking> actual = bookingService.findByUserIdAndState(booker.getId(), BookingState.ALL);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByBookerIdOrderByStartDesc(booker.getId());
    }

    @Test
    void findByUserIdAndStateShouldReturnCurrentBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(booker.getId()), eq(BookingStatus.APPROVED), any(Instant.class), any(Instant.class)))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByUserIdAndState(booker.getId(), BookingState.CURRENT);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByBookerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(booker.getId()), eq(BookingStatus.APPROVED), any(Instant.class), any(Instant.class));
    }

    @Test
    void findByUserIdAndStateShouldReturnPastBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusAndEndBeforeOrderByStartDesc(
                eq(booker.getId()), eq(BookingStatus.APPROVED), any(Instant.class)))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByUserIdAndState(booker.getId(), BookingState.PAST);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByBookerIdAndStatusAndEndBeforeOrderByStartDesc(
                eq(booker.getId()), eq(BookingStatus.APPROVED), any(Instant.class));
    }

    @Test
    void findByUserIdAndStateShouldReturnFutureBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusAndStartAfterOrderByStartDesc(
                eq(booker.getId()), eq(BookingStatus.APPROVED), any(Instant.class)))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByUserIdAndState(booker.getId(), BookingState.FUTURE);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByBookerIdAndStatusAndStartAfterOrderByStartDesc(
                eq(booker.getId()), eq(BookingStatus.APPROVED), any(Instant.class));
    }

    @Test
    void findByUserIdAndStateShouldReturnWaitingBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.WAITING))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByUserIdAndState(booker.getId(), BookingState.WAITING);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.WAITING);
    }

    @Test
    void findByUserIdAndStateShouldReturnRejectedBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.REJECTED))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByUserIdAndState(booker.getId(), BookingState.REJECTED);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.REJECTED);
    }

    @Test
    void findByUserIdAndStateShouldThrowWhenUserNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.findByUserIdAndState(booker.getId(), BookingState.ALL));

        assertEquals("User with id=2 not found", ex.getMessage());
        verify(userRepository).findById(booker.getId());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void findByOwnerIdAndStateShouldReturnAllBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(owner.getId())).thenReturn(expected);

        List<Booking> actual = bookingService.findByOwnerIdAndState(owner.getId(), BookingState.ALL);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByItemOwnerIdOrderByStartDesc(owner.getId());
    }

    @Test
    void findByOwnerIdAndStateShouldReturnCurrentBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(owner.getId()), eq(BookingStatus.APPROVED), any(Instant.class), any(Instant.class)))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByOwnerIdAndState(owner.getId(), BookingState.CURRENT);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByItemOwnerIdAndStatusAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(owner.getId()), eq(BookingStatus.APPROVED), any(Instant.class), any(Instant.class));
    }

    @Test
    void findByOwnerIdAndStateShouldReturnPastBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(
                eq(owner.getId()), eq(BookingStatus.APPROVED), any(Instant.class)))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByOwnerIdAndState(owner.getId(), BookingState.PAST);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByItemOwnerIdAndStatusAndEndBeforeOrderByStartDesc(
                eq(owner.getId()), eq(BookingStatus.APPROVED), any(Instant.class));
    }

    @Test
    void findByOwnerIdAndStateShouldReturnFutureBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(
                eq(owner.getId()), eq(BookingStatus.APPROVED), any(Instant.class)))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByOwnerIdAndState(owner.getId(), BookingState.FUTURE);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByItemOwnerIdAndStatusAndStartAfterOrderByStartDesc(
                eq(owner.getId()), eq(BookingStatus.APPROVED), any(Instant.class));
    }

    @Test
    void findByOwnerIdAndStateShouldReturnWaitingBookings() {
        List<Booking> expected = Collections.singletonList(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.WAITING))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByOwnerIdAndState(owner.getId(), BookingState.WAITING);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.WAITING);
    }

    @Test
    void findByOwnerIdAndStateShouldReturnRejectedBookings() {
        List<Booking> expected = List.of(booking);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.REJECTED))
                .thenReturn(expected);

        List<Booking> actual = bookingService.findByOwnerIdAndState(owner.getId(), BookingState.REJECTED);

        assertEquals(expected, actual);
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.REJECTED);
    }

    @Test
    void findByOwnerIdAndStateShouldThrowWhenUserNotFound() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.findByOwnerIdAndState(owner.getId(), BookingState.ALL));

        assertEquals("User with id=1 not found", ex.getMessage());
        verify(userRepository).findById(owner.getId());
        verifyNoInteractions(bookingRepository);
    }

}
