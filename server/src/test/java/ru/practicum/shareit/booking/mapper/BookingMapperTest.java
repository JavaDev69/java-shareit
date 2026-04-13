package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingMapperTest {
    private final ItemService itemService = Mockito.mock(ItemService.class);
    private final UserService userService = Mockito.mock(UserService.class);
    private BookingMapper bookingMapper;

    @BeforeEach
    void setUp() {
        DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        bookingMapper = Mappers.getMapper(BookingMapper.class);
        ReflectionTestUtils.setField(bookingMapper, "itemService", itemService);
        ReflectionTestUtils.setField(bookingMapper, "userService", userService);
        ReflectionTestUtils.setField(bookingMapper, "dateTimeMapper", dateTimeMapper);
    }

    @Test
    void toEntityShouldSetWaitingStatusAndResolveReferences() {
        BookingDto dto = new BookingDto();
        dto.setBookerId(5L);
        dto.setItemId(10L);
        dto.setStart(LocalDateTime.of(2026, 4, 13, 12, 0, 0));
        dto.setEnd(LocalDateTime.of(2026, 4, 14, 12, 0, 0));

        Item item = Item.builder().id(10L).name("drill").build();
        User user = User.builder().id(5L).name("booker").email("booker@test.local").build();

        when(itemService.getItem(10L)).thenReturn(item);
        when(userService.getUser(5L)).thenReturn(user);

        Booking actual = bookingMapper.toEntity(dto);

        assertThat(actual.getId()).isNull();
        assertThat(actual.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(actual.getItem()).isSameAs(item);
        assertThat(actual.getBooker()).isSameAs(user);
        assertThat(actual.getStart()).isEqualTo(dto.getStart().toInstant(ZoneOffset.UTC));
        assertThat(actual.getEnd()).isEqualTo(dto.getEnd().toInstant(ZoneOffset.UTC));
        verify(itemService).getItem(10L);
        verify(userService).getUser(5L);
    }

    @Test
    void toDtoShouldMapNestedObjectsAndDateTime() {
        User booker = User.builder().id(7L).name("booker").email("booker@test.local").build();
        Item item = Item.builder()
                .id(3L)
                .name("drill")
                .description("desc")
                .available(true)
                .build();
        Booking booking = Booking.builder()
                .id(20L)
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .start(Instant.parse("2026-04-13T09:00:00Z"))
                .end(Instant.parse("2026-04-14T09:00:00Z"))
                .build();

        ResponseBookingDto actual = bookingMapper.toDto(booking);

        assertThat(actual.id()).isEqualTo(20L);
        assertThat(actual.status()).isEqualTo(BookingStatus.APPROVED);
        assertThat(actual.booker().id()).isEqualTo(7L);
        assertThat(actual.item().id()).isEqualTo(3L);
        assertThat(actual.item().name()).isEqualTo("drill");
        assertThat(actual.start()).isEqualTo(LocalDateTime.of(2026, 4, 13, 9, 0));
        assertThat(actual.end()).isEqualTo(LocalDateTime.of(2026, 4, 14, 9, 0));
    }
}
