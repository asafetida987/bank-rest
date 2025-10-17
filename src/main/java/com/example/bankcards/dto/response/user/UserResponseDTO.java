package com.example.bankcards.dto.response.user;

import com.example.bankcards.entity.enums.UserRole;


public record UserResponseDTO(
        Long id,
        String login,
        UserRole role
) {
}
