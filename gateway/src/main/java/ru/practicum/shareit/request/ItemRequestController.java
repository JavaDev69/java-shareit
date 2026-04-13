package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

@Validated
@RequiredArgsConstructor
@Controller
@Slf4j
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> save(@Valid @RequestBody ItemRequestDto requestDto,
                                       @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Creating request {} for user {}", requestDto, userId);
        return itemRequestClient.saveRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Fetching own requests for user {}", userId);
        return itemRequestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Fetching all requests for user {}", userId);
        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@Valid @PositiveOrZero @PathVariable Long requestId,
                                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Fetching request {} for user {}", requestId, userId);
        return itemRequestClient.getRequest(userId, requestId);
    }
}
