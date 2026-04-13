package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    @NotBlank(message = "Обязательное поле")
    private String name;
    @Email(message = "Некорректный формат адреса электронной почты.")
    @NotBlank(message = "Обязательное поле")
    private String email;
}
