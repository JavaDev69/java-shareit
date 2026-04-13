package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController itemRequestController;

    @Test
    @DisplayName("Создание заявки на аренду с корректными данными возвращает созданную заявку")
    void saveRequest_ReturnsCreatedRequest() {
        ItemRequestDto requestDto = new ItemRequestDto("Need a drill");
        Long userId = 1L;
        ItemRequestResponseDto expectedResponse = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(Instant.now())
                .build();

        when(itemRequestService.save(any(ItemRequestDto.class), anyLong()))
                .thenReturn(expectedResponse);

        ItemRequestResponseDto response = itemRequestController.save(requestDto, userId);

        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Получение списка собственных заявок пользователя возвращает список заявок")
    void getOwnRequests_ReturnsListOfOwnRequests() {
        Long userId = 1L;
        List<ItemRequestWithItemResponseDto> expectedRequests = List.of(
                ItemRequestWithItemResponseDto.builder()
                        .id(1L)
                        .description("Request 1")
                        .created(Instant.now())
                        .items(List.of())
                        .build()
        );

        when(itemRequestService.getOwnRequests(anyLong()))
                .thenReturn(expectedRequests);

        List<ItemRequestWithItemResponseDto> response = itemRequestController.getOwnRequests(userId);

        assertEquals(expectedRequests, response);
    }

    @Test
    @DisplayName("Получение всех заявок возвращает список заявок")
    void getAllRequests_ReturnsListOfAllRequests() {
        List<ItemRequestResponseDto> expectedRequests = List.of(
                ItemRequestResponseDto.builder()
                        .id(1L)
                        .description("General request 1")
                        .created(Instant.now())
                        .build()
        );

        when(itemRequestService.getAllRequests())
                .thenReturn(expectedRequests);

        List<ItemRequestResponseDto> response = itemRequestController.getAllRequests();

        assertEquals(expectedRequests, response);
    }

    @Test
    @DisplayName("Получение заявки по ID возвращает заявку с прикрепленными элементами")
    void getRequestById_ReturnsRequestWithItems() {
        Long requestId = 1L;
        ItemRequestWithItemResponseDto expectedRequest = ItemRequestWithItemResponseDto.builder()
                .id(1L)
                .description("Request with items")
                .created(Instant.now())
                .items(List.of())
                .build();

        when(itemRequestService.getRequestById(anyLong()))
                .thenReturn(expectedRequest);

        ItemRequestWithItemResponseDto response = itemRequestController.getRequestById(requestId);

        assertEquals(expectedRequest, response);
    }

    @Test
    @DisplayName("Получение заявки по несуществующему ID выбрасывает NotFoundException")
    void getRequestById_WithInvalidId_ThrowsException() {
        Long invalidRequestId = -1L;
        when(itemRequestService.getRequestById(anyLong()))
                .thenThrow(new NotFoundException("Item request not found with id: " + invalidRequestId));

        assertThrows(NotFoundException.class, () -> itemRequestController.getRequestById(invalidRequestId));
    }
}