package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 14:39
 * @project java-shareit
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.create(user);
    }

    @Override
    public User updateUser(Long userId, UpdateUserDto user) {
        User targetUser = userRepository.findById(userId);
        if (user.hasName()) {
            targetUser.setName(user.getName());
        }
        if (user.hasEmail()) {
            targetUser.setEmail(user.getEmail());
        }
        return userRepository.update(targetUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.delete(userId);
    }
}
