package com.example.bankcards.controller;

import com.example.bankcards.dto.request.card.CardRequestDTO;
import com.example.bankcards.dto.request.card.NewCardRequestDTO;
import com.example.bankcards.dto.request.card.TransferRequestDTO;
import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.card.BalanceResponseDTO;
import com.example.bankcards.dto.response.card.CardResponseDTO;
import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.security.CurrentUserProvider;
import com.example.bankcards.service.CardApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@Validated
@RequestMapping("/api/v1/cards")
@Tag(name = "Card", description = "API для операций с картами")
@RequiredArgsConstructor
public class CardController {

    private final CardApiService cardApiService;
    private final CurrentUserProvider currentUserProvider;

    //USER

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Получение информации о картах пользователя",
            description = "Позволяет получить информацию о картах пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация о картах успешно получена",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<PagedResponseDTO<CardResponseDTO>> getMyCards(
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "100") @Min(1) Integer size,
            @RequestParam(required = false) LocalDate expiryDateFrom,
            @RequestParam(required = false) LocalDate expiryDateTo,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceFrom,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceTo,
            @RequestParam(required = false) Boolean isRequestBlock
    ) {
        User currentUser = currentUserProvider.get();
        PagedResponseDTO<CardResponseDTO> responseDTO =
                cardApiService.getMyCards(currentUser, page, size, expiryDateFrom, expiryDateTo, status, balanceFrom, balanceTo, isRequestBlock);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{cardId}/balance")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Получение информации о балансе карты пользователя",
            description = "Позволяет получить информацию о балансе карты пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация о балансе карты пользователя успешно получена",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BalanceResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или карта не принадлежит пользователю",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<BalanceResponseDTO> getBalance(
            @PathVariable @NotNull @Positive Long cardId
    ) {
        User currentUser = currentUserProvider.get();
        BalanceResponseDTO responseDTO = cardApiService.getBalanceByCard(currentUser, cardId);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Перевод средств между картами пользователя",
            description = "Позволяет перевести средства между картами пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BalanceResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или карты не принадлежат пользователю",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<MessageResponseDTO> transfer(
            @RequestBody @Valid TransferRequestDTO transferRequestDTO
    ) {
        User currentUser = currentUserProvider.get();
        MessageResponseDTO messageResponseDTO = cardApiService.transfer(currentUser, transferRequestDTO);

        return ResponseEntity.ok(messageResponseDTO);
    }

    @PostMapping("/request-block")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Запрос на блокировку карты пользователя",
            description = "Позволяет осуществить запрос на блокировку карты пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Запрос на блокировку выполнен успешно",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BalanceResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или карта не принадлежит пользователю",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<MessageResponseDTO> requestBlock(
            @RequestBody @Valid CardRequestDTO cardRequestDTO
    ) {
        User currentUser = currentUserProvider.get();
        MessageResponseDTO messageResponseDTO = cardApiService.requestBlock(currentUser, cardRequestDTO);

        return ResponseEntity.ok(messageResponseDTO);
    }

    //ADMIN

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получение информации админом о всех картах",
            description = "Позволяет получить информацию админом о всех картах",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация о картах успешно получена",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<PagedResponseDTO<CardResponseDTO>> getAllCards(
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "100") @Min(1) Integer size,
            @RequestParam(required = false) String userLogin,
            @RequestParam(required = false) LocalDate expiryDateFrom,
            @RequestParam(required = false) LocalDate expiryDateTo,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceFrom,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal balanceTo,
            @RequestParam(required = false) Boolean isRequestBlock
    ) {
        PagedResponseDTO<CardResponseDTO> responseDTO =
                cardApiService.getAllCards(page, size, userLogin, expiryDateFrom, expiryDateTo, status, balanceFrom, balanceTo, isRequestBlock);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создание новой карты",
            description = "Позволяет админу создать новую карту",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта успешно создана",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<CardResponseDTO> createNewCard(
            @RequestBody @Valid NewCardRequestDTO newCardRequestDTO
    ) {
        CardResponseDTO cardResponseDTO = cardApiService.createNewCard(newCardRequestDTO);

        return ResponseEntity.ok(cardResponseDTO);
    }

    @PatchMapping("/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Блокировка карты",
            description = "Позволяет админу заблокировать карту",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<MessageResponseDTO> blockCard(
            @RequestBody @Valid CardRequestDTO cardRequestDTO
    ) {
        MessageResponseDTO messageResponseDTO = cardApiService.blockCard(cardRequestDTO);

        return ResponseEntity.ok(messageResponseDTO);
    }

    @PatchMapping("/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Активирование карты",
            description = "Позволяет админу активировать карту",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта успешно активирована",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<MessageResponseDTO> activateCard(
            @RequestBody @Valid CardRequestDTO cardRequestDTO
    ) {
        MessageResponseDTO messageResponseDTO = cardApiService.activateCard(cardRequestDTO);

        return ResponseEntity.ok(messageResponseDTO);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удаление карты",
            description = "Позволяет админу удалить карту",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта успешно удалена",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<MessageResponseDTO> deleteCard(
            @PathVariable @NotNull @Positive Long cardId
    ) {
        MessageResponseDTO messageResponseDTO = cardApiService.deleteCard(cardId);

        return ResponseEntity.ok(messageResponseDTO);
    }

}
