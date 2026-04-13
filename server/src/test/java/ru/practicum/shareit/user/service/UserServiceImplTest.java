package ru.practicum.shareit.user.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.common.exception.EmailAlreadyExistException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.ShareItException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("user")
                .email("user@test.local")
                .build();
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        List<User> expected = List.of(user);
        when(userRepository.findAll()).thenReturn(expected);

        List<User> actual = userService.getAllUsers();

        assertEquals(expected, actual);
        verify(userRepository).findAll();
    }

    @Test
    void getUserShouldReturnUserWhenExists() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User actual = userService.getUser(user.getId());

        assertSame(user, actual);
        verify(userRepository).findById(user.getId());
    }

    @Test
    void getUserShouldThrowWhenUserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.getUser(user.getId()));

        assertEquals("Пользователь с id=1 не найден.", ex.getMessage());
        verify(userRepository).findById(user.getId());
    }

    @Test
    void saveUserShouldPersistUser() {
        when(userRepository.save(user)).thenReturn(user);

        User actual = userService.saveUser(user);

        assertSame(user, actual);
        verify(userRepository).save(user);
    }

    @Test
    void saveUserShouldThrowEmailAlreadyExistWhenConstraintUsersEmailKey() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "duplicate email",
                new ConstraintViolationException("duplicate", null, "users_email_key")
        );
        when(userRepository.save(user)).thenThrow(exception);

        EmailAlreadyExistException ex = assertThrows(
                EmailAlreadyExistException.class,
                () -> userService.saveUser(user)
        );

        assertEquals("Данный email уже занят: user@test.local", ex.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void saveUserShouldThrowShareItExceptionOnUnknownDataIntegrityViolation() {
        when(userRepository.save(user)).thenThrow(new DataIntegrityViolationException("db error"));

        ShareItException ex = assertThrows(ShareItException.class, () -> userService.saveUser(user));

        assertEquals("Ошибка сохранения пользователя.", ex.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldUpdateNameAndEmailWhenEmailIsUnique() {
        UpdateUserDto update = new UpdateUserDto();
        update.setName("new-name");
        update.setEmail("new@test.local");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsUserByEmail(update.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User actual = userService.updateUser(user.getId(), update);

        assertSame(user, actual);
        assertEquals("new-name", user.getName());
        assertEquals("new@test.local", user.getEmail());
        verify(userRepository).existsUserByEmail(update.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldNotCheckUniquenessWhenEmailDoesNotChange() {
        UpdateUserDto update = new UpdateUserDto();
        update.setEmail(user.getEmail());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User actual = userService.updateUser(user.getId(), update);

        assertSame(user, actual);
        verify(userRepository, never()).existsUserByEmail(any());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldThrowWhenEmailAlreadyExists() {
        UpdateUserDto update = new UpdateUserDto();
        update.setEmail("duplicate@test.local");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsUserByEmail(update.getEmail())).thenReturn(true);

        EmailAlreadyExistException ex = assertThrows(
                EmailAlreadyExistException.class,
                () -> userService.updateUser(user.getId(), update)
        );

        assertEquals("Данный email уже занят: duplicate@test.local", ex.getMessage());
        verify(userRepository).existsUserByEmail(update.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserShouldDelegateToRepository() {
        userService.deleteUser(user.getId());

        verify(userRepository).deleteById(user.getId());
    }
}
