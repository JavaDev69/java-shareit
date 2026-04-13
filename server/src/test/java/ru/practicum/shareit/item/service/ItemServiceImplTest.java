package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.SaveCommentException;
import ru.practicum.shareit.common.exception.UserNotSpecifiedException;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndDateDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemComment;
import ru.practicum.shareit.item.model.ItemWithBookingDate;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private DateTimeMapper dateTimeMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User otherUser;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("owner").email("owner@test.local").build();
        otherUser = User.builder().id(2L).name("other").email("other@test.local").build();
        item = Item.builder()
                .id(10L)
                .name("item")
                .description("desc")
                .available(true)
                .owner(owner)
                .build();
    }

    @Test
    void getAllItemsShouldMapItemsAndAttachOnlyOwnComments() {
        ItemWithBookingDate first = createProjection(10L, "first", "d1", true,
                Instant.parse("2026-04-10T10:00:00Z"), Instant.parse("2026-04-20T10:00:00Z"));
        ItemWithBookingDate second = createProjection(11L, "second", "d2", false,
                null, Instant.parse("2026-04-30T10:00:00Z"));
        List<ItemComment> comments = List.of(
                new ItemComment(100L, 10L, "comment-1", "alice", Instant.parse("2026-04-01T10:00:00Z")),
                new ItemComment(101L, 11L, "comment-2", "bob", Instant.parse("2026-04-02T10:00:00Z"))
        );

        when(userService.getUser(owner.getId())).thenReturn(owner);
        when(itemRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(first, second));
        when(itemRepository.findCommentsByItemIds(List.of(10L, 11L))).thenReturn(comments);

        List<ItemWithCommentsAndDateDto> actual = itemService.getAllItems(owner.getId());

        assertEquals(2, actual.size());
        assertEquals(10L, actual.get(0).id());
        assertEquals(1, actual.get(0).comments().size());
        assertEquals("comment-1", actual.get(0).comments().iterator().next().text());
        assertEquals(11L, actual.get(1).id());
        assertEquals(1, actual.get(1).comments().size());
        assertEquals("comment-2", actual.get(1).comments().iterator().next().text());
        verify(itemRepository).findCommentsByItemIds(List.of(10L, 11L));
    }

    @Test
    void getAllItemsShouldThrowWhenUserIdIsNull() {
        UserNotSpecifiedException ex = assertThrows(
                UserNotSpecifiedException.class,
                () -> itemService.getAllItems(null)
        );

        assertEquals("Пользователь не задан.", ex.getMessage());
        verifyNoInteractions(userService, itemRepository, commentRepository, bookingRepository);
    }

    @Test
    void getItemShouldReturnItemWhenExists() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        Item actual = itemService.getItem(item.getId());

        assertSame(item, actual);
        verify(itemRepository).findById(item.getId());
    }

    @Test
    void getItemShouldThrowWhenNotFound() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.getItem(item.getId()));

        assertEquals("Item with id=10 not found", ex.getMessage());
    }

    @Test
    void getItemsByTextShouldDelegateToRepository() {
        List<Item> expected = List.of(item);
        when(itemRepository.findAllByText("drill")).thenReturn(expected);

        List<Item> actual = itemService.getItemsByText("drill");

        assertEquals(expected, actual);
        verify(itemRepository).findAllByText("drill");
    }

    @Test
    void saveItemShouldSetOwnerAndPersist() {
        Item newItem = Item.builder().name("new").description("new desc").available(true).build();
        when(userService.getUser(owner.getId())).thenReturn(owner);
        when(itemRepository.save(newItem)).thenReturn(newItem);

        Item actual = itemService.saveItem(newItem, owner.getId());

        assertSame(newItem, actual);
        assertSame(owner, newItem.getOwner());
        verify(itemRepository).save(newItem);
    }

    @Test
    void updateItemShouldUpdateFieldsWhenUserIsOwner() {
        UpdateItemDto update = new UpdateItemDto();
        update.setName("updated");
        update.setDescription("updated desc");
        update.setAvailable(false);

        when(userService.getUser(owner.getId())).thenReturn(owner);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        Item actual = itemService.updateItem(item.getId(), update, owner.getId());

        assertSame(item, actual);
        assertEquals("updated", item.getName());
        assertEquals("updated desc", item.getDescription());
        assertEquals(false, item.isAvailable());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItemShouldThrowWhenUserIsNotOwner() {
        UpdateItemDto update = new UpdateItemDto();
        update.setName("updated");

        when(userService.getUser(otherUser.getId())).thenReturn(otherUser);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> itemService.updateItem(item.getId(), update, otherUser.getId())
        );

        assertEquals("Пользователь не является владельцем вещи.", ex.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void saveCommentShouldPersistCommentWhenUserCanComment() {
        Comment newComment = Comment.builder().text("great item").build();

        when(userService.getUser(owner.getId())).thenReturn(owner);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(owner.getId()), eq(item.getId()), eq(APPROVED), any(Instant.class)
        )).thenReturn(true);
        when(commentRepository.save(newComment)).thenReturn(newComment);

        Comment actual = itemService.saveComment(newComment, item.getId(), owner.getId());

        assertSame(newComment, actual);
        assertSame(owner, newComment.getAuthor());
        assertSame(item, newComment.getItem());
        verify(commentRepository).save(newComment);
    }

    @Test
    void saveCommentShouldThrowWhenUserHasNoCompletedApprovedBooking() {
        Comment newComment = Comment.builder().text("great item").build();

        when(userService.getUser(owner.getId())).thenReturn(owner);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(owner.getId()), eq(item.getId()), eq(APPROVED), any(Instant.class)
        )).thenReturn(false);

        SaveCommentException ex = assertThrows(
                SaveCommentException.class,
                () -> itemService.saveComment(newComment, item.getId(), owner.getId())
        );

        assertEquals(
                "Добавить комментарий может только пользователь, "
                        + "который брал вещь в аренду.",
                ex.getMessage()
        );
        verify(commentRepository, never()).save(any(Comment.class));
    }

    private ItemWithBookingDate createProjection(Long id,
                                                 String name,
                                                 String description,
                                                 Boolean available,
                                                 Instant last,
                                                 Instant next) {
        ItemWithBookingDate projection = org.mockito.Mockito.mock(ItemWithBookingDate.class);
        when(projection.getId()).thenReturn(id);
        when(projection.getName()).thenReturn(name);
        when(projection.getDescription()).thenReturn(description);
        when(projection.getIsAvailable()).thenReturn(available);
        when(projection.getLastBooking()).thenReturn(last);
        when(projection.getNextBooking()).thenReturn(next);
        return projection;
    }
}
