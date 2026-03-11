package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Andrew Vilkov
 * @created 09.03.2026 - 15:22
 * @project java-shareit
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User getValidUser() {
        return User.builder()
                .id(ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE))
                .name(RandomString.make())
                .email(RandomString.make() + "@test.com")
                .build();
    }

    private UserDto getValidUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    @Test
    void findAllShouldReturnUsers() throws Exception {
        User user = getValidUser();
        UserDto dto = getValidUserDto(user);
        User secondUser = getValidUser();
        UserDto secondDto = getValidUserDto(secondUser);

        when(userService.getAllUsers()).thenReturn(List.of(user, secondUser));
        when(mapper.map(user)).thenReturn(dto);
        when(mapper.map(secondUser)).thenReturn(secondDto);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(user.getId()))
                .andExpect(jsonPath("$[0].name").value(user.getName()))
                .andExpect(jsonPath("$[0].email").value(user.getEmail()))
                .andExpect(jsonPath("$[1].id").value(secondUser.getId()))
                .andExpect(jsonPath("$[1].name").value(secondUser.getName()))
                .andExpect(jsonPath("$[1].email").value(secondUser.getEmail()));
    }

    @Test
    void findOneShouldReturnUser() throws Exception {
        User user = getValidUser();
        UserDto dto = getValidUserDto(user);

        when(userService.getUser(user.getId())).thenReturn(user);
        when(mapper.map(user)).thenReturn(dto);

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void saveUserShouldCreateUser() throws Exception {
        User saved = getValidUser();
        UserDto response = getValidUserDto(saved);

        UserDto request = getValidUserDto(saved);
        request.setId(null);

        User mapped = saved.toBuilder().id(null).build();

        when(mapper.map(any(UserDto.class))).thenReturn(mapped);
        when(userService.saveUser(mapped)).thenReturn(saved);
        when(mapper.map(saved)).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value(saved.getName()))
                .andExpect(jsonPath("$.email").value(saved.getEmail()));
    }

    @Test
    void updateUserShouldReturnUpdatedUser() throws Exception {
        User updated = getValidUser();
        UserDto response = getValidUserDto(updated);

        UpdateUserDto request = new UpdateUserDto();
        request.setName(updated.getName());
        request.setEmail(updated.getEmail());

        when(userService.updateUser(eq(updated.getId()), any(UpdateUserDto.class)))
                .thenReturn(updated);
        when(mapper.map(updated)).thenReturn(response);

        mockMvc.perform(patch("/users/{id}", updated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updated.getId()))
                .andExpect(jsonPath("$.name").value(updated.getName()))
                .andExpect(jsonPath("$.email").value(updated.getEmail()));
    }

    @Test
    void deleteUserShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(1L);
    }

    @Test
    void saveUserShouldReturn400WhenInvalidEmail() throws Exception {
        UserDto request = new UserDto();
        request.setName("John");
        request.setEmail("wrong-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}