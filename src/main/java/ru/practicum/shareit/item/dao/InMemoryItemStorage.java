package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.common.SequenceIdGenerator;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 13:08
 * @project java-shareit
 */
@Log4j2
@RequiredArgsConstructor
@Repository
public class InMemoryItemStorage implements ItemRepository {
    private final SequenceIdGenerator idGenerator;
    private final Map<Long, Item> items = HashMap.newHashMap(100);

    @Override
    public Item create(Item item) {
        long nextId = idGenerator.getNextId();
        item.setId(nextId);
        items.put(nextId, item.toBuilder().build());
        log.debug("Create item {}", item);
        return item;
    }

    @Override
    public Item update(Item targetItem) {
        checkIdIsValid(targetItem.getId());
        items.replace(targetItem.getId(), targetItem.toBuilder().build());
        log.debug("Updating item {}", targetItem);
        return targetItem;
    }

    @Override
    public List<Item> findAll(Long userId) {
        return items
                .values()
                .stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(e -> e.toBuilder().build())
                .toList();
    }

    @Override
    public List<Item> findAllByText(String text) {
        return items
                .values()
                .stream()
                .filter(getPredicateByText(text))
                .filter(Item::isAvailable)
                .map(e -> e.toBuilder().build())
                .toList();
    }

    @Override
    public Item findOne(Long itemId) {
        checkIdIsValid(itemId);
        return items.get(itemId).toBuilder().build();
    }

    private Predicate<Item> getPredicateByText(String text) {
        String searchText = text.toLowerCase();
        return item -> Stream.of(item.getDescription(), item.getName())
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(e -> e.contains(searchText));
    }

    private void checkIdIsValid(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Не найдена вещь с id=%s", itemId.toString());
        }
    }
}
