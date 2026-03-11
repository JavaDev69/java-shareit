package ru.practicum.shareit.user.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.common.SequenceIdGenerator;
import ru.practicum.shareit.common.exception.EmailAlreadyExistException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 15:14
 * @project java-shareit
 */
@Log4j2
@RequiredArgsConstructor
@Repository
public class InMemoryUserStorage implements UserRepository {
    private final SequenceIdGenerator idGenerator;
    private final Map<Long, User> users = HashMap.newHashMap(100);

    @Override
    public User create(User user) {
        checkEmailIsValid(user.getEmail());
        long nextId = idGenerator.getNextId();
        user.setId(nextId);
        users.put(nextId, user.toBuilder().build());
        log.debug("Create new user {}", user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return users
                .values()
                .stream()
                .map(e -> e.toBuilder().build())
                .toList();
    }

    @Override
    public User findById(Long userId) {
        checkIdIsValid(userId);
        return users.get(userId).toBuilder().build();
    }

    @Override
    public User update(User user) {
        checkIdIsValid(user.getId());

        User userToUpdate = users.get(user.getId());

        if (!userToUpdate.getEmail().equalsIgnoreCase(user.getEmail())) {
            checkEmailIsValid(user.getEmail());
        }


        users.replace(user.getId(), user.toBuilder().build());
        log.debug("Updating user {}", user);
        return user;
    }

    @Override
    public void delete(Long userId) {
        checkIdIsValid(userId);
        users.remove(userId);
        log.debug("User with id {} has been deleted", userId);
    }

    private void checkIdIsValid(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Не найден пользователь с id=%s", userId.toString());
        }
    }

    private void checkEmailIsValid(String email) {
        boolean isExistEmail = users.values()
                .stream()
                .map(User::getEmail)
                .anyMatch(email::equals);
        if (isExistEmail) {
            throw new EmailAlreadyExistException(email);
        }
    }
}
