package ru.practicum.shareit.testdata;

import ru.practicum.shareit.item.dto.UpdateItemDto;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 16:54
 * @project java-shareit
 */
public class UpdateItemDtoTestBuilder {
    private String name = "updated-name";
    private String description = "updated-description";
    private Boolean available = true;

    public static UpdateItemDtoTestBuilder anUpdateItemDto() {
        return new UpdateItemDtoTestBuilder();
    }

    public UpdateItemDtoTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateItemDtoTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public UpdateItemDtoTestBuilder available(Boolean available) {
        this.available = available;
        return this;
    }

    public UpdateItemDto build() {
        UpdateItemDto dto = new UpdateItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }
}
