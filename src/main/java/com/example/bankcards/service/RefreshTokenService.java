package com.example.bankcards.service;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import com.example.bankcards.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh_expiration}")
    private int refreshMaxAge;

    @Transactional
    public RefreshToken create(User user) {
        log.info("Создание refresh токена для пользователя id={}", user.getId());
        RefreshToken refreshToken = buildRefreshToken(user);
        log.info("Refresh токен создан для пользователя id={}", user.getId());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public User getUserByToken(String token){
        log.info("Получение пользователя по refresh токену {}", token);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh токен {} не найден", token);
                    return new UserNotAuthenticatedException("Refresh токен не найден");
                });
        validate(refreshToken);
        log.info("Пользователь id={} успешно получен по refresh токену", refreshToken.getUser().getId());

        return refreshToken.getUser();

    }

    @Transactional
    public void delete(String token){
        log.info("Удаление refresh токена {}", token);
        refreshTokenRepository.deleteRefreshTokenByToken(token);
        log.info("Refresh токен {} удален", token);
    }

    public void deleteByExpiration(Instant expiration){
        log.info("Удаление истекших refresh токенов до {}", expiration);
        refreshTokenRepository.deleteRefreshTokenByExpiryDateBefore(expiration);
        log.info("Истекшие refresh токены удалены");
    }

    private void validate(RefreshToken refreshToken){
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh токен {} для пользователя id={} истек", refreshToken.getToken(), refreshToken.getUser().getId());
            refreshTokenRepository.deleteRefreshTokenByToken(refreshToken.getToken());
            throw new UserNotAuthenticatedException("Refresh токен истек");
        }
    }

    private RefreshToken buildRefreshToken(User user){
        return RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshMaxAge))
                .build();
    }
}
