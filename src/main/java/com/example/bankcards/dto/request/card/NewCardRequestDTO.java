package com.example.bankcards.dto.request.card;

import jakarta.validation.constraints.NotBlank;

public record NewCardRequestDTO(
        @NotBlank(message = "Логин владельца карты не может быть пустым") String ownerLogin
) {
}
