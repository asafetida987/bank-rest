package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenCleanupService {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredTokens(){
        Instant now = Instant.now();
        refreshTokenService.deleteByExpiration(now);
    }
}
