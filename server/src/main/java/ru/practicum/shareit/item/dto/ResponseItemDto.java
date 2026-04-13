package ru.practicum.shareit.item.dto;

public record ResponseItemDto(Long id,
                              String name,
                              String description,
                              Boolean available) {

}
