package com.example.bankcards.dto.request.card;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequestDTO(
        @NotNull(message = "id карты не может быть пустым")
        @Positive(message = "id карты должен быть положительным")
        Long cardIdFrom,
        @NotNull(message = "id карты не может быть пустым")
        @Positive(message = "id карты должен быть положительным")
        Long cardIdTo,
        @NotNull(message = "Сумма перевода не может быть пустой")
        @DecimalMin(value = "0.01", message = "Минимальная сумма перевода — 0.01")
        BigDecimal amount
) {
}
