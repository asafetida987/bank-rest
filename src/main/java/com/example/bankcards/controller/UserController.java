package com.example.bankcards.controller;

import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

//    @GetMapping("/{userId}")
//    public ResponseEntity<UserResponseDTO> getUser(
//            @PathVariable @NotNull @Positive Long userId
//    ) {
//
//    }
//
//    @DeleteMapping("/{userId}")
//    public ResponseEntity<MessageResponseDTO> deleteUser(
//            @PathVariable @NotNull @Positive Long userId
//    ) {
//
//    }
//
//    @GetMapping("/all")
//    public ResponseEntity<PagedResponseDTO<UserResponseDTO>> getAllUsers(
//            @RequestParam(required = false) @Min(0) Integer page,
//            @RequestParam(required = false) @Min(1) Integer size,
//            @RequestParam(required = false) String login,
//            @RequestParam(required = false) LocalDate createdAtFrom,
//            @RequestParam(required = false) LocalDate createdAtTo
//            ) {
//
//    }
}
