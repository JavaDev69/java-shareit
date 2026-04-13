package ru.practicum.shareit.testdata;

import ru.practicum.shareit.item.dto.ItemDto;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 16:54
 * @project java-shareit
 */
public class ItemDtoTestBuilder {
    private Long id = 1L;
    private String name = "item";
    private String description = "description";
    private Boolean available = true;

    public static ItemDtoTestBuilder anItemDto() {
        return new ItemDtoTestBuilder();
    }

    public ItemDtoTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ItemDtoTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ItemDtoTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemDtoTestBuilder available(Boolean available) {
        this.available = available;
        return this;
    }

    public ItemDto build() {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }
}
