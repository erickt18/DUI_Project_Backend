package com.rfidcampus.rfid_campus.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rfidcampus.rfid_campus.dto.ForgotPasswordRequest;
import com.rfidcampus.rfid_campus.dto.ResetPasswordRequest;
import com.rfidcampus.rfid_campus.model.PasswordResetToken;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.PasswordResetTokenRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestReset(ForgotPasswordRequest req) {
        System.out.println("🔍 Buscando usuario: " + req.getEmail());
        
        Usuario usuario = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("✅ Usuario encontrado: " + usuario.getNombreCompleto());

        // Eliminar tokens anteriores del usuario
        tokenRepo.deleteByUsuarioId(usuario.getId()); // ✅ CORREGIDO: getId() en lugar de getIdUsuario()
        System.out.println("🗑️ Tokens anteriores eliminados");

        // Generar nuevo token
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        tokenRepo.save(resetToken);
        System.out.println("✅ Token de recuperación generado para: " + usuario.getEmail());

        // Enviar email
        emailService.sendPasswordResetEmail(usuario.getEmail(), token);
        System.out.println("✅ Email de recuperación enviado a: " + usuario.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        System.out.println("🔍 Validando token: " + req.getToken());
        
        PasswordResetToken token = tokenRepo.findByToken(req.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (token.isUsed()) {
            throw new RuntimeException("El token ya fue utilizado");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        Usuario usuario = token.getUsuario();
        
        // ✅ ENCRIPTAR LA CONTRASEÑA CON BCRYPT
        usuario.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        usuarioRepo.save(usuario);

        token.setUsed(true);
        tokenRepo.save(token);
        
        System.out.println("✅ Contraseña actualizada para: " + usuario.getEmail());
    }

    public boolean isTokenValid(String tokenStr) {
        return tokenRepo.findByToken(tokenStr)
                .map(token -> !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
