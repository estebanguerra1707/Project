package com.mx.mitienda.repository;

import com.mx.mitienda.model.PasswordResetToken;
import com.mx.mitienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);
    Optional<PasswordResetToken> findByUsuario(Usuario usuario);
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    void deleteAllExpired(LocalDateTime now);
}
