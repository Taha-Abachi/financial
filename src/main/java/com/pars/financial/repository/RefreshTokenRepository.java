package com.pars.financial.repository;

import com.pars.financial.entity.RefreshToken;
import com.pars.financial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUser(User user);
    
    List<RefreshToken> findByUserAndIsRevokedFalse(User user);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt, rt.revokeReason = :reason WHERE rt.user = :user AND rt.isRevoked = false")
    void revokeAllUserTokens(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt, @Param("reason") String reason);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiredBefore")
    void deleteExpiredTokens(@Param("expiredBefore") LocalDateTime expiredBefore);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.isRevoked = false")
    long countActiveTokensByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiresAt < :expiredBefore")
    long countByExpiresAtBefore(@Param("expiredBefore") LocalDateTime expiredBefore);
}
