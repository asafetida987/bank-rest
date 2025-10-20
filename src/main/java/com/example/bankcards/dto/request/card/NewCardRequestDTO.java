package com.example.bankcards.dto.request.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NewCardRequestDTO(
        @NotNull(message = "id пользователя не может быть пустым")
        @Positive(message = "id пользователя должен быть положительным")
        Long ownerId
) {
}
