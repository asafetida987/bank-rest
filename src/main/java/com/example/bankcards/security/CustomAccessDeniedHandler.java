package com.example.bankcards.security;

import com.example.bankcards.exception.ExceptionResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Обработчик отказа в доступе для Spring Security.
 * Реализует {@link AccessDeniedHandler} и формирует JSON-ответ с кодом 403 и сообщением о
 * том, что пользователь не авторизован для доступа к запрашиваемому ресурсу.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Обрабатывает отказ в доступе пользователя к ресурсу.
     *
     * @param request  объект HTTP-запроса
     * @param response объект HTTP-ответа
     * @param accessDeniedException исключение, вызванное отказом в доступе
     * @throws IOException если возникает ошибка при записи JSON-ответа
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Отказ в доступе для пользователя на URL {}", request.getRequestURI());

        ExceptionResponseDTO dto = new ExceptionResponseDTO(HttpStatus.FORBIDDEN, "Пользователь не авторизован");

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }
}
