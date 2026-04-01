package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ResponseCommentDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.ResponseItemWithCommentDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper mapper;
    private final CommentMapper commentMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseItemWithCommentDto> findAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService
                .getAllItems(userId)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseItemWithCommentDto findOne(@PathVariable Long itemId) {
        Item findedItem = itemService.getItem(itemId);
        return mapper.mapWithComment(findedItem);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseItemDto> search(@RequestParam(name = "text") String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        return itemService
                .getItemsByText(text)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseItemDto saveItem(@RequestBody @Valid ItemDto item,
                                    @RequestHeader(USER_ID_HEADER) Long userId) {
        Item newItem = mapper.map(item);
        Item saved = itemService.saveItem(newItem, userId);
        return mapper.map(saved);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseItemDto updateItem(@PathVariable Long itemId,
                                      @RequestBody @Valid UpdateItemDto item,
                                      @RequestHeader(USER_ID_HEADER) Long userId) {
        Item updatedItem = itemService.updateItem(itemId, item, userId);
        return mapper.map(updatedItem);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCommentDto saveComment(@RequestBody @Valid CommentDto comment,
                                          @PathVariable Long itemId,
                                          @RequestHeader(USER_ID_HEADER) Long userId) {
        Comment newComment = commentMapper.map(comment);
        Comment saved = itemService.saveComment(newComment, itemId, userId);
        return commentMapper.map(saved);
    }
}
