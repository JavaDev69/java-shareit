package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateUserDtoTest {

    @Test
    void hasNameShouldReturnFalseWhenNameNull() {
        UpdateUserDto dto = new UpdateUserDto();

        assertThat(dto.hasName()).isFalse();
    }

    @Test
    void hasNameShouldReturnTrueWhenNamePresent() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("John");

        assertThat(dto.hasName()).isTrue();
    }

    @Test
    void hasEmailShouldReturnFalseWhenEmailNull() {
        UpdateUserDto dto = new UpdateUserDto();

        assertThat(dto.hasEmail()).isFalse();
    }

    @Test
    void hasEmailShouldReturnTrueWhenEmailPresent() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setEmail("john@test.com");

        assertThat(dto.hasEmail()).isTrue();
    }
}
