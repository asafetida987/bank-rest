package com.example.bankcards.dto.request.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CardRequestDTO(
        @NotNull(message = "id карты не может быть пустым")
        @Positive(message = "id карты должен быть положительным")
        Long cardId
) {
}
