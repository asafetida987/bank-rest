package com.example.bankcards.service;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import com.example.bankcards.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

    @Value("${jwt.access_expiration}")
    private int accessMaxAge;
    @Value("${jwt.refresh_expiration}")
    private int refreshMaxAge;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Transactional
    public void addAuthCookies(HttpServletResponse response, Long userId, boolean rememberMe){
        log.info("Добавление cookies для пользователя id={}, rememberMe={}", userId, rememberMe);
        User user = userService.findUserById(userId);
        String accessToken = jwtTokenProvider.createAccessToken(user);
        addAccessTokenCookie(response, accessToken, accessMaxAge);
        log.info("Access cookie добавлена для пользователя id={}", userId);
        if (rememberMe){
            RefreshToken refreshToken = refreshTokenService.create(user);
            String refresh = jwtTokenProvider.createRefreshToken(refreshToken);
            addRefreshTokenCookie(response, refresh, refreshMaxAge);
            log.info("Refresh cookie добавлена для пользователя id={}", userId);
        }

    }

    public void deleteAuthCookies(HttpServletRequest request, HttpServletResponse response){
        String refreshToken = extractRefreshToken(request);
        log.info("Удаление cookies");
        if (refreshToken != null) {
            String refreshUUID = jwtTokenProvider.extractUUIDRefreshToken(refreshToken);
            refreshTokenService.delete(refreshUUID);
            log.info("Refresh токен с UUID={} удален", refreshUUID);
        }
        addAccessTokenCookie(response, "", 0);
        addRefreshTokenCookie(response, "", 0);
        log.info("Cookies успешно удалены");
    }

    @Transactional
    public void refreshAuthCookies(HttpServletRequest request, HttpServletResponse response){
        log.info("Обновление cookies пользователя");
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            log.warn("Попытка обновления cookies без refresh токена");
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }
        String refreshUUID = jwtTokenProvider.extractUUIDRefreshToken(refreshToken);
        User user = refreshTokenService.getUserByToken(refreshUUID);
        String accessToken = jwtTokenProvider.createAccessToken(user);
        addAccessTokenCookie(response, accessToken, accessMaxAge);
        addRefreshTokenCookie(response, refreshToken, refreshMaxAge);
        log.info("Cookies успешно обновлены для пользователя id={}", user.getId());

    }

    private void addAccessTokenCookie(HttpServletResponse response, String token, int maxAge) {
        addCookie(response, "access_token", token, maxAge);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token, int maxAge) {
        addCookie(response, "refresh_token", token, maxAge);
    }

    private void addCookie(HttpServletResponse response, String name, String token, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.warn("Нет cookies в запросе");
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refresh_token"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

    }
}
