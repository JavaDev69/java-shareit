package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemRequestClient itemRequestClient;

    @Test
    void saveShouldCreateRequest() throws Exception {
        ItemRequestDto request = new ItemRequestDto("Need a drill");

        when(itemRequestClient.saveRequest(eq(1L), any(ItemRequestDto.class))).thenReturn(ResponseEntity.status(201).body(
                Map.of("id", 1L, "description", "Need a drill")
        ));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOwnRequestsShouldReturnRequests() throws Exception {
        when(itemRequestClient.getOwnRequests(1L)).thenReturn(ResponseEntity.ok(List.of(
                Map.of("id", 1L, "description", "Need a drill")
        )));

        mockMvc.perform(get("/requests").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllRequestsShouldReturnRequests() throws Exception {
        when(itemRequestClient.getAllRequests(1L)).thenReturn(ResponseEntity.ok(List.of(
                Map.of("id", 2L, "description", "Need a saw")
        )));

        mockMvc.perform(get("/requests/all").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Need a saw"));
    }

    @Test
    void getRequestByIdShouldReturnRequest() throws Exception {
        when(itemRequestClient.getRequest(1L, 5L)).thenReturn(ResponseEntity.ok(
                Map.of("id", 5L, "description", "Need a ladder")
        ));

        mockMvc.perform(get("/requests/5").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getRequestByIdShouldReturn400WhenIdNegative() throws Exception {
        mockMvc.perform(get("/requests/-1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getRequest(anyLong(), any(Long.class));
    }
}
