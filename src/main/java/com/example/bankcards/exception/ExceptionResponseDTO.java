package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

public record ExceptionResponseDTO(
        OffsetDateTime exceptionTimestamp,
        HttpStatus status,
        String message
        ) {

        public ExceptionResponseDTO(HttpStatus status, String message) {
                this(OffsetDateTime.now(), status, message);
        }
}
