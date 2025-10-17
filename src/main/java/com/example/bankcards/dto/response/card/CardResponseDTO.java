package com.example.bankcards.dto.response.card;

import java.time.LocalDate;

public record CardResponseDTO(
        Long id,
        String number,
        String ownerLogin,
        LocalDate expiryDate,
        String cardStatus
) {
}
