package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduledService {

    private final RefreshTokenService refreshTokenService;
    private final CardAdminService cardAdminService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredTokens(){
        Instant now = Instant.now();
        refreshTokenService.deleteByExpiration(now);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateStatusForExpiredCard(){
        LocalDate now = LocalDate.now();
        cardAdminService.updateForExpiredCard(now);

    }
}
