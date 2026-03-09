package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 14:32
 * @project java-shareit
 */
public interface ItemRepository {
    Item create(Item item);

    List<Item> findAll(Long userId);

    Item findOne(Long itemId);

    List<Item> findAllByText(String text);

    Item update(Item targetItem);
}
