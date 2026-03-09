package ru.practicum.shareit.testdata;

import ru.practicum.shareit.item.model.Item;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 16:53
 * @project java-shareit
 */
public class ItemTestBuilder {
    private Long id = 1L;
    private String name = "item";
    private String description = "description";
    private boolean available = true;

    public static ItemTestBuilder anItem() {
        return new ItemTestBuilder();
    }

    public ItemTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ItemTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ItemTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemTestBuilder available(boolean available) {
        this.available = available;
        return this;
    }

    public Item build() {
        return Item.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .build();
    }
}
