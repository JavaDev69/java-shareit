package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateItemDtoTest {

    @Test
    void hasNameShouldReturnFalseWhenNameNull() {
        UpdateItemDto dto = new UpdateItemDto();

        assertThat(dto.hasName()).isFalse();
    }

    @Test
    void hasNameShouldReturnTrueWhenNamePresent() {
        UpdateItemDto dto = new UpdateItemDto();
        dto.setName("Drill");

        assertThat(dto.hasName()).isTrue();
    }

    @Test
    void hasDescriptionShouldReturnFalseWhenDescriptionNull() {
        UpdateItemDto dto = new UpdateItemDto();

        assertThat(dto.hasDescription()).isFalse();
    }

    @Test
    void hasDescriptionShouldReturnTrueWhenDescriptionPresent() {
        UpdateItemDto dto = new UpdateItemDto();
        dto.setDescription("Good");

        assertThat(dto.hasDescription()).isTrue();
    }

    @Test
    void hasAvailableShouldReturnFalseWhenAvailableNull() {
        UpdateItemDto dto = new UpdateItemDto();

        assertThat(dto.hasAvailable()).isFalse();
    }

    @Test
    void hasAvailableShouldReturnTrueWhenAvailablePresent() {
        UpdateItemDto dto = new UpdateItemDto();
        dto.setAvailable(true);

        assertThat(dto.hasAvailable()).isTrue();
    }
}
