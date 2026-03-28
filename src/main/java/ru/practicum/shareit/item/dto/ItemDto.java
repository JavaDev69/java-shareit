package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDto {
    private Long id;
    @NotBlank(message = "Обязательное поле")
    private String name;
    @NotBlank(message = "Обязательное поле")
    private String description;
    @NotNull(message = "Обязательное поле")
    private Boolean available;
}
