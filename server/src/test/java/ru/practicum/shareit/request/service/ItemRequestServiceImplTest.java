package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private RequestRepository itemRequestRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("user").email("user@test.local").build();
    }

    @Test
    void saveShouldMapPersistAndReturnResponseDto() {
        ItemRequestDto requestDto = new ItemRequestDto("Need a drill");
        ItemRequest mapped = ItemRequest.builder().description(requestDto.description()).build();
        ItemRequest saved = ItemRequest.builder().id(10L).description(requestDto.description()).requestor(user).build();
        ItemRequestResponseDto response = ItemRequestResponseDto.builder()
                .id(10L)
                .description("Need a drill")
                .created(Instant.parse("2026-04-12T10:00:00Z"))
                .build();

        when(userService.getUser(user.getId())).thenReturn(user);
        when(itemRequestMapper.map(requestDto)).thenReturn(mapped);
        when(itemRequestRepository.save(mapped)).thenReturn(saved);
        when(itemRequestMapper.toResponseDto(saved)).thenReturn(response);

        ItemRequestResponseDto actual = itemRequestService.save(requestDto, user.getId());

        assertSame(response, actual);
        assertSame(user, mapped.getRequestor());
        verify(itemRequestRepository).save(mapped);
    }

    @Test
    void getRequestByIdShouldReturnMappedDtoWhenExists() {
        ItemRequest request = ItemRequest.builder().id(10L).description("desc").build();
        ItemRequestWithItemResponseDto response = ItemRequestWithItemResponseDto.builder()
                .id(10L)
                .description("desc")
                .created(Instant.parse("2026-04-12T10:00:00Z"))
                .items(Collections.emptyList())
                .build();

        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toResponseWithItemDto(request)).thenReturn(response);

        ItemRequestWithItemResponseDto actual = itemRequestService.getRequestById(10L);

        assertSame(response, actual);
    }

    @Test
    void getRequestByIdShouldThrowWhenNotFound() {
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(10L));

        assertEquals("Item request not found with id: 10", ex.getMessage());
    }

    @Test
    void getOwnRequestsShouldReturnDtosSortedByCreatedAscending() {
        ItemRequest firstRequest = ItemRequest.builder().id(10L).description("first").build();
        ItemRequest secondRequest = ItemRequest.builder().id(11L).description("second").build();

        ItemRequestWithItemResponseDto late = ItemRequestWithItemResponseDto.builder()
                .id(10L)
                .description("first")
                .created(Instant.parse("2026-04-12T12:00:00Z"))
                .items(Collections.emptyList())
                .build();
        ItemRequestWithItemResponseDto early = ItemRequestWithItemResponseDto.builder()
                .id(11L)
                .description("second")
                .created(Instant.parse("2026-04-12T11:00:00Z"))
                .items(Collections.emptyList())
                .build();

        when(userService.getUser(user.getId())).thenReturn(user);
        when(itemRequestRepository.findByRequestor(user)).thenReturn(List.of(firstRequest, secondRequest));
        when(itemRequestMapper.toResponseWithItemDto(firstRequest)).thenReturn(late);
        when(itemRequestMapper.toResponseWithItemDto(secondRequest)).thenReturn(early);

        List<ItemRequestWithItemResponseDto> actual = itemRequestService.getOwnRequests(user.getId());

        assertEquals(List.of(early, late), actual);
    }

    @Test
    void getAllRequestsShouldReturnMappedResponseDtos() {
        ItemRequest request = ItemRequest.builder().id(10L).description("desc").build();
        ItemRequestResponseDto response = ItemRequestResponseDto.builder()
                .id(10L)
                .description("desc")
                .created(Instant.parse("2026-04-12T10:00:00Z"))
                .build();

        when(itemRequestRepository.findByItemsEmpty()).thenReturn(List.of(request));
        when(itemRequestMapper.toResponseDto(request)).thenReturn(response);

        List<ItemRequestResponseDto> actual = itemRequestService.getAllRequests();

        assertEquals(List.of(response), actual);
    }
}
