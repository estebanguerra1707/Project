package com.mx.mitienda.scheduler;

import com.mx.mitienda.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanUpScheduler {
    private final PasswordResetTokenRepository tokenRepository;
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Cada hora
    public void cleanExpiredTokens() {
        tokenRepository.deleteAllExpired(LocalDateTime.now());
    }
}
