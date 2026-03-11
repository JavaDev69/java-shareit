package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.UserNotSpecifiedException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 13:17
 * @project java-shareit
 */
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public List<Item> getAllItems(Long userId) {
        User owner = checkUserIsValidAndGet(userId);
        return itemRepository.findAll(owner.getId());
    }

    @Override
    public Item getItem(Long itemId) {
        return itemRepository.findOne(itemId);
    }

    @Override
    public List<Item> getItemsByText(String text) {
        return itemRepository.findAllByText(text);
    }

    @Override
    public Item saveItem(Item newItem, Long userId) {
        User owner = checkUserIsValidAndGet(userId);
        newItem.setOwner(owner);
        return itemRepository.create(newItem);
    }

    @Override
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
        return itemRepository.update(targetItem);
    }

    private User checkUserIsValidAndGet(Long userId) {
        if (userId == null) {
            throw new UserNotSpecifiedException();
        }
        return userService.getUser(userId);
    }
}
