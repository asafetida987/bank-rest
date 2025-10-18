package com.example.bankcards.service;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import com.example.bankcards.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh_expiration}")
    private int refreshMaxAge;

    @Transactional
    public RefreshToken create(User user) {
        RefreshToken refreshToken = buildRefreshToken(user);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public User getUserByToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UserNotAuthenticatedException("Refresh токен не найден"));
        validate(refreshToken);

        return refreshToken.getUser();

    }

    @Transactional
    public void delete(String token){

        refreshTokenRepository.deleteRefreshTokenByToken(token);
    }

    public void deleteByExpiration(Instant expiration){

        refreshTokenRepository.deleteRefreshTokenByExpiryDateBefore(expiration);
    }

    private void validate(RefreshToken refreshToken){
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
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
