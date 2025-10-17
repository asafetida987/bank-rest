package com.example.bankcards.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Логин не может быть пустым")
        String login,
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Пароль должен содержать хотя бы одну заглавную букву, строчную букву и цифру"
        )
        String password,
        boolean rememberMe
) {
}
