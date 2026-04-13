package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndDateDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.ResponseItemWithCommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemComment;
import ru.practicum.shareit.item.model.ItemWithBookingDate;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RequestRepository requestRepository;

    private ItemMapper itemMapper;

    @BeforeEach
    void setUp() {
        DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

        ReflectionTestUtils.setField(commentMapper, "dateTimeMapper", dateTimeMapper);

        itemMapper = Mappers.getMapper(ItemMapper.class);
        ReflectionTestUtils.setField(itemMapper, "commentMapper", commentMapper);
        ReflectionTestUtils.setField(itemMapper, "dateTimeMapper", dateTimeMapper);
        ReflectionTestUtils.setField(itemMapper, "bookingRepository", bookingRepository);
        ReflectionTestUtils.setField(itemMapper, "requestRepository", requestRepository);
    }

    @Test
    void mapItemToResponseItemDtoShouldMapAllFields() {
        Item item = Item.builder()
                .id(10L)
                .name("drill")
                .description("powerful")
                .available(true)
                .build();

        ResponseItemDto actual = itemMapper.map(item);

        assertThat(actual.id()).isEqualTo(10L);
        assertThat(actual.name()).isEqualTo("drill");
        assertThat(actual.description()).isEqualTo("powerful");
        assertThat(actual.available()).isTrue();
    }

    @Test
    void mapProjectionToResponseItemDtoShouldMapAvailability() {
        ItemWithBookingDate projection = mock(ItemWithBookingDate.class);
        when(projection.getId()).thenReturn(11L);
        when(projection.getName()).thenReturn("saw");
        when(projection.getDescription()).thenReturn("metal saw");
        when(projection.getIsAvailable()).thenReturn(false);

        ResponseItemDto actual = itemMapper.map(projection);

        assertThat(actual.id()).isEqualTo(11L);
        assertThat(actual.name()).isEqualTo("saw");
        assertThat(actual.description()).isEqualTo("metal saw");
        assertThat(actual.available()).isFalse();
    }

    @Test
    void mapItemDtoToItemShouldMapWithoutRequestWhenRequestIdIsNull() {
        ItemDto dto = new ItemDto();
        dto.setName("drill");
        dto.setDescription("powerful");
        dto.setAvailable(true);
        dto.setRequestId(null);

        Item actual = itemMapper.map(dto);

        assertThat(actual.getId()).isNull();
        assertThat(actual.getName()).isEqualTo("drill");
        assertThat(actual.getDescription()).isEqualTo("powerful");
        assertThat(actual.isAvailable()).isTrue();
        assertThat(actual.getRequest()).isNull();
        verifyNoInteractions(requestRepository);
    }

    @Test
    void mapItemDtoToItemShouldResolveRequestWhenRequestIdProvided() {
        ItemDto dto = new ItemDto();
        dto.setName("drill");
        dto.setDescription("powerful");
        dto.setAvailable(true);
        dto.setRequestId(1L);

        User user = User.builder().id(2L).name("owner").email("owner@test.local").build();
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(user)
                .created(Instant.parse("2026-04-13T08:00:00Z"))
                .items(Collections.emptyList())
                .build();

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        Item actual = itemMapper.map(dto);

        assertThat(actual.getId()).isNull();
        assertThat(actual.getRequest()).isSameAs(request);
        verify(requestRepository).findById(1L);
    }

    @Test
    void mapWithCommentShouldMapBookingsAndComments() {
        User author = User.builder().id(1L).name("alice").email("alice@test.local").build();
        Comment comment = Comment.builder()
                .id(31L)
                .text("great")
                .author(author)
                .created(Instant.parse("2026-04-12T12:00:00Z"))
                .build();
        Item item = Item.builder()
                .id(15L)
                .name("bike")
                .description("city")
                .available(true)
                .comments(Set.of(comment))
                .build();

        Instant lastBooking = Instant.parse("2026-04-10T10:00:00Z");
        Instant nextBooking = Instant.parse("2026-04-20T10:00:00Z");

        when(bookingRepository.getLastBookingByItemId(15L)).thenReturn(lastBooking);
        when(bookingRepository.getNextBookingByItemId(15L)).thenReturn(nextBooking);

        ResponseItemWithCommentDto actual = itemMapper.mapWithComment(item);

        assertThat(actual.id()).isEqualTo(15L);
        assertThat(actual.lastBooking()).isEqualTo(LocalDateTime.of(2026, 4, 10, 10, 0, 0));
        assertThat(actual.nextBooking()).isEqualTo(LocalDateTime.of(2026, 4, 20, 10, 0, 0));
        assertThat(actual.comments()).hasSize(1);
        assertThat(actual.comments().iterator().next().authorName()).isEqualTo("alice");
    }

    @Test
    void mapWithCommentShouldKeepBookingDatesNullWhenRepositoryReturnsNull() {
        Item item = Item.builder()
                .id(16L)
                .name("bike")
                .description("city")
                .available(true)
                .comments(Collections.emptySet())
                .build();

        when(bookingRepository.getLastBookingByItemId(16L)).thenReturn(null);
        when(bookingRepository.getNextBookingByItemId(16L)).thenReturn(null);

        ResponseItemWithCommentDto actual = itemMapper.mapWithComment(item);

        assertThat(actual.lastBooking()).isNull();
        assertThat(actual.nextBooking()).isNull();
        assertThat(actual.comments()).isEmpty();
    }

    @Test
    void mapItemWithCommentsAndDateDtoShouldMapDatesAndComments() {
        ItemComment comment = new ItemComment(
                22L,
                7L,
                "looks good",
                "bob",
                Instant.parse("2026-04-11T11:00:00Z")
        );
        ItemWithCommentsAndDateDto source = ItemWithCommentsAndDateDto.builder()
                .id(7L)
                .name("ladder")
                .description("big ladder")
                .available(true)
                .lastBooking(Instant.parse("2026-04-10T09:00:00Z"))
                .nextBooking(Instant.parse("2026-04-14T09:00:00Z"))
                .comments(Set.of(comment))
                .build();

        ResponseItemWithCommentDto actual = itemMapper.map(source);

        assertThat(actual.id()).isEqualTo(7L);
        assertThat(actual.lastBooking()).isEqualTo(LocalDateTime.of(2026, 4, 10, 9, 0, 0));
        assertThat(actual.nextBooking()).isEqualTo(LocalDateTime.of(2026, 4, 14, 9, 0, 0));
        assertThat(actual.comments()).hasSize(1);
        assertThat(actual.comments().iterator().next().text()).isEqualTo("looks good");
        assertThat(actual.comments().iterator().next().authorName()).isEqualTo("bob");
    }
}
