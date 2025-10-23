package com.example.bankcards.security;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Провайдер JWT токенов для аутентификации и авторизации пользователей.
 * Отвечает за создание, проверку и извлечение данных из access и refresh токенов.
 * Использует {@link CustomUserDetailsService} для получения данных пользователя.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.access_secret}")
    private String accessSecretKey;

    @Value("${jwt.refresh_secret}")
    private String refreshSecretKey;

    @Value("${jwt.access_expiration}")
    private long accessMaxAge;

    private final CustomUserDetailsService userDetailsService;

    /**
     * Инициализация секретных ключей после создания бина.
     * Преобразует их в Base64 для использования при подписи JWT.
     */
    @PostConstruct
    protected void init() {
        accessSecretKey = Base64.getEncoder().encodeToString(accessSecretKey.getBytes());
        refreshSecretKey = Base64.getEncoder().encodeToString(refreshSecretKey.getBytes());
    }

    /**
     * Создает access токен для пользователя.
     *
     * @param user пользователь
     * @return JWT access токен
     */
    public String createAccessToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessMaxAge);

        return Jwts.builder()
                .claim("login", user.getLogin())
                .issuedAt(now)
                .expiration(validity)
                .signWith(getSigningKey(accessSecretKey))
                .compact();
    }

    /**
     * Создает refresh токен для заданного {@link RefreshToken}.
     *
     * @param refreshToken refresh токен
     * @return JWT refresh токен
     */
    public String createRefreshToken(RefreshToken refreshToken) {
        return Jwts.builder()
                .claim("uuid", refreshToken.getToken())
                .expiration(Date.from(refreshToken.getExpiryDate()))
                .signWith(getSigningKey(refreshSecretKey))
                .compact();
    }

    /**
     * Валидирует access токен.
     *
     * @param token access токен
     * @return true, если токен валиден, иначе false
     */
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey(accessSecretKey))
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Извлекает логин пользователя из access токена.
     *
     * @param token access токен
     * @return логин пользователя
     */
    public String extractLogin(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(accessSecretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("login", String.class);
    }

    /**
     * Извлекает UUID refresh токена из JWT.
     *
     * @param token refresh токен
     * @return UUID токена
     */
    public String extractUUIDRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(refreshSecretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("uuid", String.class);
    }

    /**
     * Получает объект аутентификации {@link Authentication} из access токена.
     *
     * @param token access токен
     * @return объект аутентификации Spring Security
     */
    public Authentication getAuthentication(String token) {
        String email = extractLogin(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

}
