package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserClient userClient;

    @Test
    void findAllShouldReturnUsers() throws Exception {
        when(userClient.getUsers()).thenReturn(ResponseEntity.ok(List.of(
                Map.of("id", 1L, "name", "John", "email", "john@test.com"),
                Map.of("id", 2L, "name", "Ann", "email", "ann@test.com")
        )));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].email").value("ann@test.com"));
    }

    @Test
    void findOneShouldReturnUser() throws Exception {
        when(userClient.getUser(1L)).thenReturn(ResponseEntity.ok(
                Map.of("id", 1L, "name", "John", "email", "john@test.com")
        ));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void saveUserShouldCreateUser() throws Exception {
        UserDto request = new UserDto();
        request.setName("John");
        request.setEmail("john@test.com");

        when(userClient.saveUser(any(UserDto.class))).thenReturn(ResponseEntity.status(201).body(
                Map.of("id", 1L, "name", "John", "email", "john@test.com")
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateUserShouldReturnUpdatedUser() throws Exception {
        UpdateUserDto request = new UpdateUserDto();
        request.setName("John Updated");

        when(userClient.updateUser(eq(1L), any(UpdateUserDto.class))).thenReturn(ResponseEntity.ok(
                Map.of("id", 1L, "name", "John Updated", "email", "john@test.com")
        ));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
    }

    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        when(userClient.deleteUser(1L)).thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
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

        verify(userClient, never()).saveUser(any(UserDto.class));
    }
}
