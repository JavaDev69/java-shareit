package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Comparator;
import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 06.04.2026 - 19:34
 * @project java-shareit
 */
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final RequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final UserService userService;

    @Override
    public ItemRequestResponseDto save(ItemRequestDto requestDto, Long userId) {
        User currentUser = userService.getUser(userId);

        ItemRequest itemRequest = itemRequestMapper.map(requestDto);
        itemRequest.setRequestor(currentUser);

        ItemRequest saved = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toResponseDto(saved);
    }

    @Override
    public ItemRequestWithItemResponseDto getRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request not found with id: " + requestId));
        return itemRequestMapper.toResponseWithItemDto(itemRequest);
    }

    @Override
    public List<ItemRequestWithItemResponseDto> getOwnRequests(Long userId) {
        User currentUser = userService.getUser(userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequestor(currentUser);

        return requests.stream()
                .map(itemRequestMapper::toResponseWithItemDto)
                .sorted(Comparator.comparing(ItemRequestWithItemResponseDto::created))
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests() {
        List<ItemRequest> requests = itemRequestRepository.findByItemsEmpty();
        return requests.stream()
                .map(itemRequestMapper::toResponseDto)
                .toList();
    }
}
