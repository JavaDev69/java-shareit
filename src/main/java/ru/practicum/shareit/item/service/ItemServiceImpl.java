package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.SaveCommentException;
import ru.practicum.shareit.common.exception.UserNotSpecifiedException;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 13:17
 * @project java-shareit
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Override
    public List<ItemWithCommentsAndDateDto> getAllItems(Long userId) {
        User owner = checkUserIsValidAndGet(userId);
        List<ItemWithBookingDate> allByOwnerId = itemRepository.findAllByOwnerId(owner.getId());

        List<Long> itemIds = allByOwnerId.stream().map(ItemWithBookingDate::getId).toList();
        List<ItemComment> comments = itemRepository.findCommentsByItemIds(itemIds);

        return allByOwnerId.stream()
                .map(e -> ItemWithCommentsAndDateDto.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .description(e.getDescription())
                        .comments(comments.stream()
                                .filter(c -> c.itemId().equals(e.getId()))
                                .collect(Collectors.toSet()))
                        .available(e.getIsAvailable())
                        .lastBooking(e.getLastBooking())
                        .nextBooking(e.getNextBooking())
                        .build())
                .toList();
    }

    @Override
    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=%d not found", itemId));
    }

    @Override
    public List<Item> getItemsByText(String text) {
        return itemRepository.findAllByText(text);
    }

    @Override
    @Transactional
    public Item saveItem(Item newItem, Long userId) {
        User owner = checkUserIsValidAndGet(userId);
        newItem.setOwner(owner);
        return itemRepository.save(newItem);
    }

    @Override
    @Transactional
    public Item updateItem(Long itemId, UpdateItemDto item, Long userId) {
        User user = checkUserIsValidAndGet(userId);
        Item targetItem = getItem(itemId);
        if (!user.equals(targetItem.getOwner())) {
            throw new ForbiddenException("Пользователь не является владельцем вещи.");
        }
        if (item.hasName()) {
            targetItem.setName(item.getName());
        }
        if (item.hasDescription()) {
            targetItem.setDescription(item.getDescription());
        }
        if (item.hasAvailable()) {
            targetItem.setAvailable(item.getAvailable());
        }
        return itemRepository.save(targetItem);
    }

    @Override
    @Transactional
    public Comment saveComment(Comment newComment, Long itemId, Long userId) {
        User user = checkUserIsValidAndGet(userId);
        Item targetItem = getItem(itemId);
        checkUserCanWriteCommentForItem(itemId, userId);
        newComment.setItem(targetItem);
        newComment.setAuthor(user);
        return commentRepository.save(newComment);
    }

    private void checkUserCanWriteCommentForItem(Long itemId, Long userId) {
        boolean isCan = bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId, APPROVED, LocalDateTime.now().toInstant(ZoneOffset.UTC));
        if (!isCan) {
            throw new SaveCommentException("Добавить комментарий может только пользователь, который брал вещь в аренду.");
        }

    }

    private User checkUserIsValidAndGet(Long userId) {
        if (userId == null) {
            throw new UserNotSpecifiedException();
        }
        return userService.getUser(userId);
    }
}
