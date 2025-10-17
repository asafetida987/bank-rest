package com.example.bankcards.controller;

import com.example.bankcards.dto.request.card.CardRequestDTO;
import com.example.bankcards.dto.request.card.NewCardRequestDTO;
import com.example.bankcards.dto.request.card.TransferRequestDTO;
import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.card.BalanceResponseDTO;
import com.example.bankcards.dto.response.card.CardResponseDTO;
import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@Validated
@RequestMapping("/api/v1/cards")
public class CardController {

    //USER

//    @GetMapping
//    public ResponseEntity<PagedResponseDTO<CardResponseDTO>> getMyCards(
//            @RequestParam(required = false) @Min(0) Integer page,
//            @RequestParam(required = false) @Min(1) Integer size,
//            @RequestParam(required = false) LocalDate expiryDateFrom,
//            @RequestParam(required = false) LocalDate expiryDateTo,
//            @RequestParam(required = false) CardStatus status,
//            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceFrom,
//            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceTo,
//            @RequestParam(required = false) Boolean isRequestBlock
//    ) {
//
//    }
//
//    @GetMapping("/{cardId}/balance")
//    public ResponseEntity<BalanceResponseDTO> getBalance(
//            @PathVariable @NotNull @Positive Long cardId
//    ) {
//
//    }
//
//    @PostMapping("/transfer")
//    public ResponseEntity<MessageResponseDTO> transfer(
//            @RequestBody @Valid TransferRequestDTO transferRequestDTO
//    ) {
//
//    }
//
//    @PostMapping("/request-block")
//    public ResponseEntity<MessageResponseDTO> requestBlock(
//            @RequestBody @Valid CardRequestDTO cardRequestDTO
//    ) {
//
//    }
//
//    //ADMIN
//
//    @GetMapping("/all")
//    public ResponseEntity<PagedResponseDTO<CardResponseDTO>> getAllCards(
//            @RequestParam(required = false) @Min(0) Integer page,
//            @RequestParam(required = false) @Min(1) Integer size,
//            @RequestParam(required = false) String userLogin,
//            @RequestParam(required = false) LocalDate expiryDateFrom,
//            @RequestParam(required = false) LocalDate expiryDateTo,
//            @RequestParam(required = false) CardStatus status,
//            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceFrom,
//            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceTo,
//            @RequestParam(required = false) Boolean isRequestBlock
//    ) {
//
//    }
//
//    @PostMapping("/create")
//    public ResponseEntity<CardResponseDTO> createNewCard(
//            @RequestBody @Valid NewCardRequestDTO newCardRequestDTO
//    ) {
//
//    }
//
//    @PatchMapping("/block")
//    public ResponseEntity<MessageResponseDTO> blockCard(
//            @RequestBody @Valid CardRequestDTO cardRequestDTO
//    ) {
//
//    }
//
//    @PatchMapping("/activate")
//    public ResponseEntity<MessageResponseDTO> activateCard(
//            @RequestBody @Valid CardRequestDTO cardRequestDTO
//    ) {
//
//    }
//
//    @DeleteMapping("/{cardId}")
//    public ResponseEntity<MessageResponseDTO> removeCard(
//            @PathVariable @NotNull @Positive Long cardId
//    ) {
//
//    }

}
