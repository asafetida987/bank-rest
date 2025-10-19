package com.example.bankcards.controller;

import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.service.UserApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@Validated
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "API для операций с пользователями")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserApiService userApiService;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Получение информации о пользователе",
            description = "Позволяет получить информацию о пользователе по id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации id",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<UserResponseDTO> getUser(
            @PathVariable @NotNull @Positive Long userId
    ) {
        UserResponseDTO responseDTO = userApiService.getUserById(userId);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Удаление пользователя",
            description = "Позволяет удалить пользователя по id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно удален",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации id",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<MessageResponseDTO> deleteUser(
            @PathVariable @NotNull @Positive Long userId
    ) {
        userApiService.deleteUser(userId);

        return ResponseEntity.ok(new MessageResponseDTO("Пользователь удален успешно"));
    }

    @GetMapping("/all")
    @Operation(
            summary = "Получение списка пользователей",
            description = "Позволяет получить список пользователей по фильтру и с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<PagedResponseDTO<UserResponseDTO>> getAllUsers(
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "100") @Min(1) Integer size,
            @RequestParam(required = false) String login,
            @RequestParam(required = false) LocalDate createdAtFrom,
            @RequestParam(required = false) LocalDate createdAtTo
            ) {
        PagedResponseDTO<UserResponseDTO> responseDTO = userApiService.findAllUsers(page, size, login, createdAtFrom, createdAtTo);

        return ResponseEntity.ok(responseDTO);
    }
}
