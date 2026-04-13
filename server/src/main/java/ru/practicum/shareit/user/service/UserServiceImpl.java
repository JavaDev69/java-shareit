package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exception.EmailAlreadyExistException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.ShareItException;
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
@Log4j2
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден.", userId));
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage(), e);
            if (e.getCause() instanceof ConstraintViolationException ve
                    && "users_email_key".equals(ve.getConstraintName())) {
                throw new EmailAlreadyExistException(user.getEmail());
            }
            throw new ShareItException("Ошибка сохранения пользователя.");
        }
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UpdateUserDto user) {
        User targetUser = getUser(userId);
        if (user.hasName()) {
            targetUser.setName(user.getName());
        }
        if (user.hasEmail() && !user.getEmail().equals(targetUser.getEmail())) {
            boolean isNonUniqEmail = userRepository.existsUserByEmail(user.getEmail());
            if (isNonUniqEmail) {
                throw new EmailAlreadyExistException(user.getEmail());
            }
            targetUser.setEmail(user.getEmail());
        }

        return userRepository.save(targetUser);
    }


    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
