package com.pars.financial.service;

import com.pars.financial.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    public TokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime expiredBefore = LocalDateTime.now();
            long deletedCount = refreshTokenRepository.countByExpiresAtBefore(expiredBefore);
            
            if (deletedCount > 0) {
                refreshTokenRepository.deleteExpiredTokens(expiredBefore);
                logger.info("Cleaned up {} expired refresh tokens", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
