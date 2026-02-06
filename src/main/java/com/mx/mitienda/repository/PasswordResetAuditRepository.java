package com.mx.mitienda.repository;

import com.mx.mitienda.model.PasswordResetAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PasswordResetAuditRepository extends JpaRepository<PasswordResetAudit, Long> {
    @Query("""
        SELECT COUNT(a)
        FROM PasswordResetAudit a
        WHERE a.ipAddress = :ip
          AND a.requestedAt >= :since
    """)
    long countRecentByIp(@Param("ip") String ip,
                         @Param("since") LocalDateTime since);

    @Query("""
        SELECT MIN(a.requestedAt)
        FROM PasswordResetAudit a
        WHERE a.ipAddress = :ip
          AND a.requestedAt >= :since
    """)
    LocalDateTime findOldestAttemptInWindowByIp(@Param("ip") String ip,
                                                @Param("since") LocalDateTime since);
}
