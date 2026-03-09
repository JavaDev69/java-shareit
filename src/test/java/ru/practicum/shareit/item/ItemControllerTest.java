package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.testdata.ItemDtoTestBuilder;
import ru.practicum.shareit.testdata.ItemTestBuilder;
import ru.practicum.shareit.testdata.UpdateItemDtoTestBuilder;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 15:28
 * @project java-shareit
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ItemMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findAllShouldReturnItems() throws Exception {
        Item item = ItemTestBuilder.anItem().build();
        ItemDto dto = ItemDtoTestBuilder.anItemDto().build();

        when(itemService.getAllItems(1L)).thenReturn(List.of(item));
        when(mapper.map(item)).thenReturn(dto);

        mockMvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(dto.getName()));
    }

    @Test
    void findOneShouldReturnItem() throws Exception {
        Item item = ItemTestBuilder.anItem().build();
        ItemDto dto = ItemDtoTestBuilder.anItemDto().build();

        when(itemService.getItem(1L)).thenReturn(item);
        when(mapper.map(item)).thenReturn(dto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()));
    }

    @Test
    void searchShouldReturnItems() throws Exception {
        String itemName = "drill";
        Item item = ItemTestBuilder.anItem()
                .withName(itemName)
                .build();

        ItemDto dto = ItemDtoTestBuilder.anItemDto()
                .withName(itemName)
                .build();

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
        ItemDto response = ItemDtoTestBuilder.anItemDto().withId(1L).build();

        when(mapper.map(any(ItemDto.class))).thenReturn(mapped);
        when(itemService.saveItem(mapped, 1L)).thenReturn(saved);
        when(mapper.map(saved)).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
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

        ItemDto response = ItemDtoTestBuilder.anItemDto()
                .withName(updatedItemName)
                .build();

        when(itemService.updateItem(eq(1L), any(UpdateItemDto.class), eq(1L)))
                .thenReturn(updated);

        when(mapper.map(updated)).thenReturn(response);

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedItemName));
    }
}