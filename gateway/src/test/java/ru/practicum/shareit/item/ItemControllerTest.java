package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemClient itemClient;

    @Test
    void findAllShouldReturnItems() throws Exception {
        when(itemClient.getItems(1L)).thenReturn(ResponseEntity.ok(List.of(
                Map.of("id", 1L, "name", "Drill", "description", "Good", "available", true)
        )));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void findOneShouldReturnItem() throws Exception {
        when(itemClient.getItem(1L, 2L)).thenReturn(ResponseEntity.ok(
                Map.of("id", 2L, "name", "Saw", "description", "Sharp", "available", true)
        ));

        mockMvc.perform(get("/items/2").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Saw"));
    }

    @Test
    void searchShouldReturnItems() throws Exception {
        when(itemClient.getItems(1L, "drill")).thenReturn(ResponseEntity.ok(List.of(
                Map.of("id", 1L, "name", "Drill", "description", "Good", "available", true)
        )));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void searchShouldReturn400WhenTextIsBlank() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", ""))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getItems(eq(1L), any(String.class));
    }

    @Test
    void saveItemShouldCreateItem() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Good");
        request.setAvailable(true);

        when(itemClient.saveItem(eq(1L), any(ItemDto.class))).thenReturn(ResponseEntity.status(201).body(
                Map.of("id", 1L, "name", "Drill", "description", "Good", "available", true)
        ));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateItemShouldReturnUpdatedItem() throws Exception {
        UpdateItemDto request = new UpdateItemDto();
        request.setName("Drill Pro");

        when(itemClient.updateItem(eq(1L), eq(1L), any(UpdateItemDto.class))).thenReturn(ResponseEntity.ok(
                Map.of("id", 1L, "name", "Drill Pro", "description", "Good", "available", true)
        ));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill Pro"));
    }

    @Test
    void saveCommentShouldCreateComment() throws Exception {
        CommentDto request = new CommentDto("Great item");

        when(itemClient.saveComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(ResponseEntity.status(201).body(
                Map.of("id", 1L, "text", "Great item")
        ));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great item"));
    }
}
