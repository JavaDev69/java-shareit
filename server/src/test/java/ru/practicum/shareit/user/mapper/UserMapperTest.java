package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void mapUserToDtoShouldCopyAllFields() {
        User user = User.builder()
                .id(1L)
                .name("alice")
                .email("alice@test.local")
                .build();

        UserDto actual = mapper.map(user);

        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getName()).isEqualTo("alice");
        assertThat(actual.getEmail()).isEqualTo("alice@test.local");
    }

    @Test
    void mapUserDtoToEntityShouldIgnoreId() {
        UserDto dto = new UserDto();
        dto.setId(100L);
        dto.setName("bob");
        dto.setEmail("bob@test.local");

        User actual = mapper.map(dto);

        assertThat(actual.getId()).isNull();
        assertThat(actual.getName()).isEqualTo("bob");
        assertThat(actual.getEmail()).isEqualTo("bob@test.local");
    }
}
