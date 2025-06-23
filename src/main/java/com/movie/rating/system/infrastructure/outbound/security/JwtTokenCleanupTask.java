package com.movie.rating.system.infrastructure.outbound.security;

import com.movie.rating.system.domain.port.outbound.JwtTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Scheduled task for JWT token cleanup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenCleanupTask {

    private final JwtTokenRepository jwtTokenRepository;

    @Value("${app.jwt.cleanup.retention-period:P30D}")
    private Duration retentionPeriod;

    /**
     * Clean up expired tokens every hour
     * Removes tokens that have been expired for more than the retention period
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        log.info("Starting JWT token cleanup task");
        
        Instant cutoffTime = Instant.now().minus(retentionPeriod);
        
        jwtTokenRepository.deleteExpiredTokens(cutoffTime)
                .subscribe(
                        deletedCount -> {
                            if (deletedCount > 0) {
                                log.info("Cleaned up {} expired JWT tokens", deletedCount);
                            } else {
                                log.debug("No expired JWT tokens to clean up");
                            }
                        },
                        error -> log.error("Failed to clean up expired JWT tokens", error)
                );
    }
}
