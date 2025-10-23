package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Сервис для плановых задач (scheduled tasks).
 * Обрабатывает автоматические операции, такие как удаление истекших refresh токенов
 * и обновление статуса просроченных карт.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScheduledService {

    private final RefreshTokenService refreshTokenService;
    private final CardAdminService cardAdminService;

    /**
     * Плановая задача для удаления всех истекших refresh токенов.
     * Запускается ежедневно в полночь.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredTokens(){
        Instant now = Instant.now();
        log.info("Запуск задачи удаления истекших refresh токенов");
        refreshTokenService.deleteByExpiration(now);
        log.info("Удаление истекших refresh токенов выполнено");
    }

    /**
     * Плановая задача для обновления статуса всех просроченных карт на EXPIRED.
     * Запускается ежедневно в полночь.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateStatusForExpiredCard(){
        LocalDate now = LocalDate.now();
        log.info("Запуск задачи обновления статуса истекших карт на {}", now);
        cardAdminService.updateForExpiredCard(now);
        log.info("Обновление статуса истекших карт выполнено");

    }
}
