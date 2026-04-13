package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemResponseDto;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 06.04.2026 - 19:24
 * @project java-shareit
 */
public interface ItemRequestService {
    ItemRequestWithItemResponseDto getRequestById(Long requestId);

    ItemRequestResponseDto save(ItemRequestDto requestDto, Long userId);

    List<ItemRequestWithItemResponseDto> getOwnRequests(Long userId);

    List<ItemRequestResponseDto> getAllRequests();
}
