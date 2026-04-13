package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemForRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {
    private final ItemRequestMapper mapper = Mappers.getMapper(ItemRequestMapper.class);

    @Test
    void mapDtoToEntityShouldSetDescriptionAndCreated() {
        ItemRequestDto dto = new ItemRequestDto("Need a drill");

        ItemRequest actual = mapper.map(dto);

        assertThat(actual.getDescription()).isEqualTo("Need a drill");
        assertThat(actual.getCreated()).isNotNull();
    }

    @Test
    void toResponseDtoShouldMapMainFields() {
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need a drill")
                .created(Instant.parse("2026-04-13T10:00:00Z"))
                .build();

        ItemRequestResponseDto actual = mapper.toResponseDto(request);

        assertThat(actual.id()).isEqualTo(10L);
        assertThat(actual.description()).isEqualTo("Need a drill");
        assertThat(actual.created()).isEqualTo(Instant.parse("2026-04-13T10:00:00Z"));
    }

    @Test
    void toResponseWithItemDtoShouldMapItems() {
        User owner = User.builder().id(7L).name("owner").email("owner@test.local").build();
        Item item = Item.builder().id(3L).name("drill").owner(owner).build();
        ItemRequest request = ItemRequest.builder()
                .id(11L)
                .description("Need tool")
                .created(Instant.parse("2026-04-13T11:00:00Z"))
                .items(Collections.singletonList(item))
                .build();

        ItemRequestWithItemResponseDto actual = mapper.toResponseWithItemDto(request);

        assertThat(actual.id()).isEqualTo(11L);
        assertThat(actual.items()).hasSize(1);
        ItemForRequest mappedItem = actual.items().getFirst();
        assertThat(mappedItem.id()).isEqualTo(3L);
        assertThat(mappedItem.name()).isEqualTo("drill");
        assertThat(mappedItem.ownerId()).isEqualTo(7L);
    }

    @Test
    void mapItemToItemForRequestShouldMapOwnerId() {
        User owner = User.builder().id(5L).name("owner").email("owner@test.local").build();
        Item item = Item.builder().id(2L).name("screwdriver").owner(owner).build();

        ItemForRequest actual = mapper.map(item);

        assertThat(actual.id()).isEqualTo(2L);
        assertThat(actual.name()).isEqualTo("screwdriver");
        assertThat(actual.ownerId()).isEqualTo(5L);
    }
}
