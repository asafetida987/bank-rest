package com.example.bankcards.security;

import com.example.bankcards.exception.ExceptionResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Обработчик неаутентифицированного доступа для Spring Security.
 * Реализует {@link AuthenticationEntryPoint} и формирует JSON-ответ с кодом 401 и сообщением о
 * том, что пользователь не аутентифицирован для доступа к запрашиваемому ресурсу.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Обрабатывает попытку доступа неаутентифицированного пользователя к ресурсу.
     *
     * @param request  объект HTTP-запроса
     * @param response объект HTTP-ответа
     * @param authException исключение аутентификации
     * @throws IOException если возникает ошибка при записи JSON-ответа
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.warn("Неаутентифицированный доступ к URL {}: {}", request.getRequestURI(), authException.getMessage());

        ExceptionResponseDTO dto = new ExceptionResponseDTO(HttpStatus.UNAUTHORIZED, "Пользователь не аутентифицирован");

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(dto));

    }
}
