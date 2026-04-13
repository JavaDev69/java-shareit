package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 14:38
 * @project java-shareit
 */
public interface UserService {
    List<User> getAllUsers();

    User getUser(Long userId);

    User saveUser(User user);

    User updateUser(Long userId, UpdateUserDto user);

    void deleteUser(Long userId);
}
