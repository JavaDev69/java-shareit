package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndDateDto;
import ru.practicum.shareit.item.dto.ResponseCommentDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.ResponseItemWithCommentDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.testdata.ItemDtoTestBuilder;
import ru.practicum.shareit.testdata.ItemTestBuilder;
import ru.practicum.shareit.testdata.UpdateItemDtoTestBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 15:28
 * @project java-shareit
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private ItemMapper mapper;

    @MockitoBean
    private CommentMapper commentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findAllShouldReturnItems() throws Exception {
        Item item = ItemTestBuilder.anItem().build();
        ItemWithCommentsAndDateDto itemWithComment = ItemWithCommentsAndDateDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .comments(Collections.emptySet())
                .available(item.isAvailable())
                .build();

        ResponseItemWithCommentDto dto =
                new ResponseItemWithCommentDto(
                        1L,
                        "name",
                        "desc",
                        true,
                        null,
                        null,
                        Set.of()
                );

        when(itemService.getAllItems(1L)).thenReturn(List.of(itemWithComment));
        when(mapper.map(itemWithComment)).thenReturn(dto);

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(dto.name()));
    }

    @Test
    void findOneShouldReturnItem() throws Exception {
        Item item = ItemTestBuilder.anItem().build();

        ResponseItemWithCommentDto dto =
                new ResponseItemWithCommentDto(
                        1L,
                        "name",
                        "desc",
                        true,
                        null,
                        null,
                        Set.of()
                );

        when(itemService.getItem(1L)).thenReturn(item);
        when(mapper.mapWithComment(item)).thenReturn(dto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.id()));
    }

    @Test
    void searchShouldReturnItems() throws Exception {
        String itemName = "drill";

        Item item = ItemTestBuilder.anItem()
                .withName(itemName)
                .build();

        ResponseItemDto dto =
                new ResponseItemDto(1L, itemName, "desc", true);

        when(itemService.getItemsByText(itemName)).thenReturn(List.of(item));
        when(mapper.map(item)).thenReturn(dto);

        mockMvc.perform(get("/items/search").param("text", itemName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(itemName));
    }

    @Test
    void searchShouldReturnEmptyWhenTextEmpty() throws Exception {
        mockMvc.perform(get("/items/search").param("text", ""))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void saveItemShouldCreateItem() throws Exception {
        ItemDto request = ItemDtoTestBuilder.anItemDto().build();

        Item mapped = ItemTestBuilder.anItem().build();
        Item saved = ItemTestBuilder.anItem().withId(1L).build();

        ResponseItemDto response =
                new ResponseItemDto(1L, "name", "desc", true);

        when(mapper.map(any(ItemDto.class))).thenReturn(mapped);
        when(itemService.saveItem(mapped, 1L)).thenReturn(saved);
        when(mapper.map(saved)).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateItemShouldReturnUpdatedItem() throws Exception {
        String updatedItemName = "updated";

        UpdateItemDto request = UpdateItemDtoTestBuilder.anUpdateItemDto()
                .withName(updatedItemName)
                .build();

        Item updated = ItemTestBuilder.anItem()
                .withName(updatedItemName)
                .build();

        ResponseItemDto response =
                new ResponseItemDto(1L, updatedItemName, "desc", true);

        when(itemService.updateItem(eq(1L), any(UpdateItemDto.class), eq(1L)))
                .thenReturn(updated);

        when(mapper.map(updated)).thenReturn(response);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedItemName));
    }

    @Test
    void saveCommentShouldCreateComment() throws Exception {
        CommentDto request = new CommentDto("text");

        Comment mapped = Comment.builder().text("text").build();
        Comment saved = Comment.builder()
                .id(1L)
                .text("text")
                .build();

        ResponseCommentDto response =
                new ResponseCommentDto(1L, "text", "author", null);

        when(commentMapper.map(any(CommentDto.class))).thenReturn(mapped);
        when(itemService.saveComment(mapped, 1L, 1L)).thenReturn(saved);
        when(commentMapper.map(saved)).thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("text"));
    }
}