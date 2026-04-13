package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.common.RequestHeaders;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

@Validated
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Fetching items by user {}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findOne(@RequestHeader(RequestHeaders.USER_ID_HEADER) long userId,
                                          @Valid @PositiveOrZero @PathVariable Long itemId) {
        log.info("Fetching item {} by user {}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(RequestHeaders.USER_ID_HEADER) long userId,
                                         @Valid @NotBlank @RequestParam(name = "text") String text) {
        log.info("Fetching item by text '{}' by user {}", text, userId);
        return itemClient.getItems(userId, text);
    }

    @PostMapping
    public ResponseEntity<Object> saveItem(@RequestBody @Valid ItemDto item,
                                           @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Creating item {} by user {}", item, userId);
        return itemClient.saveItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@Valid @PositiveOrZero @PathVariable Long itemId,
                                             @RequestBody @Valid UpdateItemDto requestDto,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Updating item {} to {} by user {}", itemId, requestDto, userId);
        return itemClient.updateItem(userId, itemId, requestDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> saveComment(@RequestBody @Valid CommentDto requestDto,
                                              @PathVariable Long itemId,
                                              @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Creating comment {} for item {} by user {}", requestDto, itemId, userId);
        return itemClient.saveComment(userId, itemId, requestDto);
    }
}
