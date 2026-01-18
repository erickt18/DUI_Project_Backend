package com.rfidcampus.rfid_campus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
}