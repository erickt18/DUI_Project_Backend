// repository/PasswordResetTokenRepository.java
package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);
}
