package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр аутентификации JWT, выполняемый один раз на каждый запрос.
 * Извлекает access token из cookies, проверяет его валидность с помощью {@link JwtTokenProvider}
 * и устанавливает аутентификацию в {@link SecurityContextHolder} при успешной проверке.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Выполняет фильтрацию запроса.
     * Извлекает JWT из cookies, валидирует его и устанавливает
     * аутентификацию пользователя в контекст безопасности Spring Security.
     *
     * @param request  HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException в случае ошибок сервлета
     * @throws IOException      в случае ошибок ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveTokenFromCookies(request);
        if (token != null && jwtTokenProvider.validateAccessToken(token)) {
            SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT access token из cookies запроса.
     *
     * @param request HTTP-запрос
     * @return значение токена или {@code null}, если токен отсутствует
     */
    private String resolveTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
