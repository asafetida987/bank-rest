package com.example.bankcards.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ExceptionResponseDTO> handleUserNotAuthenticatedException(UserNotAuthenticatedException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponseDTO(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> String.format("Поле '%s': %s", e.getField(), e.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponseDTO(HttpStatus.BAD_REQUEST, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponseDTO> handleMethodArgumentNotValidException(ConstraintViolationException ex) {

        String errors = ex.getConstraintViolations()
                .stream()
                .map(e -> String.format("Поле '%s': %s", e.getPropertyPath(), e.getMessage()))
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponseDTO(HttpStatus.BAD_REQUEST, errors));
    }

    @ExceptionHandler(WrongParameterException.class)
    public ResponseEntity<ExceptionResponseDTO> handleWrongParameterException(WrongParameterException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponseDTO(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponseDTO(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleGenericException(Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
