package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 14:29
 * @project java-shareit
 */
public interface UserRepository {
    User create(User user);

    List<User> findAll();

    User findById(Long userId);

    User update(User user);

    void delete(Long id);
}
