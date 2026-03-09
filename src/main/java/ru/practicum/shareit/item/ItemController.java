package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper mapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> findAll(@RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        return itemService
                .getAllItems(userId)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto findOne(@PathVariable Long itemId) {
        Item findedItem = itemService.getItem(itemId);
        return mapper.map(findedItem);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> search(@RequestParam(name = "text") String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        return itemService
                .getItemsByText(text)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto saveUser(@RequestBody @Valid ItemDto item,
                            @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        Item newItem = mapper.map(item);
        Item saved = itemService.saveItem(newItem, userId);
        return mapper.map(saved);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody @Valid UpdateItemDto item,
                              @RequestHeader(name = "X-Sharer-User-Id") Long userId) {
        Item updatedItem = itemService.updateItem(itemId, item, userId);
        return mapper.map(updatedItem);
    }
}
