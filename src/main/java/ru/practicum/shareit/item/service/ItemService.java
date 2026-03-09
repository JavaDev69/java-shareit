package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 13:15
 * @project java-shareit
 */
public interface ItemService {
    List<Item> getAllItems(Long userId);

    Item saveItem(Item newItem, Long userId);

    Item getItem(Long itemId);

    List<Item> getItemsByText(String text);

    Item updateItem(Long itemId, UpdateItemDto item, Long userId);
}
