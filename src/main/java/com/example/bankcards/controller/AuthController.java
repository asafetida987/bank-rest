package com.example.bankcards.controller;

import com.example.bankcards.dto.request.auth.LoginRequestDTO;
import com.example.bankcards.dto.request.auth.RegisterRequestDTO;
import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер, отвечающий за аутентификацию и авторизацию пользователей.
 * Содержит эндпоинты для входа, регистрации, выхода и обновления токенов.
 */
@RestController
@Validated
@RequestMapping("/api/v1/auth")
@Tag(name = "Authorization", description = "API для регистрации и входа")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    /**
     * Выполняет аутентификацию пользователя по логину и паролю.
     * В случае успеха устанавливает в ответ cookies с токенами.
     *
     * @param requestDTO DTO с данными для входа (логин, пароль, rememberMe)
     * @param response   HTTP-ответ, в который будут добавлены cookies
     * @return {@link UserResponseDTO} с данными аутентифицированного пользователя
     */
    @PostMapping("/login")
    @Operation(
            summary = "Вход пользователя",
            description = "Позволяет осуществить вход пользователю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных запроса",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<UserResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO requestDTO,
            HttpServletResponse response
    ) {
        log.info("Попытка входа пользователя с login={}", requestDTO.login());
        UserResponseDTO responseDTO = authService.login(requestDTO);
        cookieService.addAuthCookies(response, responseDTO.id(), requestDTO.rememberMe());
        log.info("Пользователь с id={} успешно вошел", responseDTO.id());

        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Регистрирует нового пользователя и сразу выполняет вход.
     * В случае успеха устанавливает в ответ cookies с токенами.
     *
     * @param requestDTO DTO с данными для создания учетной записи
     * @param response   HTTP-ответ, в который будут добавлены cookies
     * @return {@link UserResponseDTO} с данными зарегистрированного пользователя
     */
    @PostMapping("/register")
    @Operation(
            summary = "Регистрация пользователя",
            description = "Позволяет зарегистироваться пользователю",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных запроса",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json"))
            })
    public ResponseEntity<UserResponseDTO> register(
            @RequestBody @Valid RegisterRequestDTO requestDTO,
            HttpServletResponse response
    ) {
        log.info("Регистрация нового пользователя с login={}", requestDTO.login());
        UserResponseDTO responseDTO = authService.register(requestDTO);
        cookieService.addAuthCookies(response, responseDTO.id(), requestDTO.rememberMe());
        log.info("Пользователь с id={} успешно зарегистрирован", responseDTO.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

    }

    /**
     * Выполняет выход пользователя — удаляет cookies с токенами.
     *
     * @param request  HTTP-запрос, из которого будут извлечены cookies
     * @param response HTTP-ответ, в который вернется пустой набор cookies
     * @return текстовое подтверждение выхода
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Выход пользователя",
            description = "Позволяет осуществить выход пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный выход"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<MessageResponseDTO> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Выход пользователя");
        cookieService.deleteAuthCookies(request, response);
        log.info("Выход успешно выполнен");

        return ResponseEntity.ok(new MessageResponseDTO("Выход осуществлен успешно"));
    }

    /**
     * Обновляет access-токен по refresh-токену из cookies.
     *
     * @param request  HTTP-запрос с refresh-token cookie
     * @param response HTTP-ответ, в который будет добавлен новый access-token
     * @return текстовое подтверждение обновления токена
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Обновление токена",
            description = "Позволяет обновить access токен",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешно"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<MessageResponseDTO> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Обновление токена пользователя");
        cookieService.refreshAuthCookies(request, response);
        log.info("Токен успешно обновлен");

        return ResponseEntity.ok(new MessageResponseDTO("Токен обновлен успешно"));
    }
}
