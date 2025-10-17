package com.example.bankcards.dto.response.card;

import java.math.BigDecimal;

public record BalanceResponseDTO(
        BigDecimal balance
) {
}
