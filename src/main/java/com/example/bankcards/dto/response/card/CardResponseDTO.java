package com.example.bankcards.dto.response.card;

import com.example.bankcards.entity.enums.CardStatus;

import java.time.LocalDate;

public record CardResponseDTO(
        Long id,
        String number,
        String ownerLogin,
        LocalDate expiryDate,
        CardStatus cardStatus
) {
}
