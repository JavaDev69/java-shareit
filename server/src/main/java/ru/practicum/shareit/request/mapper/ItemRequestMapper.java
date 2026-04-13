package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemForRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * @author Andrew Vilkov
 * @created 06.04.2026 - 21:35
 * @project java-shareit
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {
                DateTimeMapper.class
        },
        imports = {
                ZoneId.class,
                Instant.class,
                ArrayList.class
        })
public interface ItemRequestMapper {

    ItemRequestResponseDto toResponseDto(ItemRequest request);

    @Mapping(target = "created", expression = "java(Instant.now())")
    ItemRequest map(ItemRequestDto dto);

    ItemRequestWithItemResponseDto toResponseWithItemDto(ItemRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    ItemForRequest map(Item item);
}
