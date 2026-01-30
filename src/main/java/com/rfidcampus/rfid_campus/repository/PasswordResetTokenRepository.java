package com.rfidcampus.rfid_campus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rfidcampus.rfid_campus.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.usuario.id = :usuarioId") // ✅ CORREGIDO: .id en lugar de .idUsuario
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}
