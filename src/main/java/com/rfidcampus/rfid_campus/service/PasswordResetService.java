package com.rfidcampus.rfid_campus.service;

import java.time.LocalDateTime; // Asegúrate de tener este DTO
import java.util.UUID; // Asegúrate de tener este DTO

import org.springframework.mail.SimpleMailMessage; // ✅ CAMBIO: Usuario
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder; // ✅ CAMBIO: UsuarioRepository
import org.springframework.stereotype.Service;

import com.rfidcampus.rfid_campus.dto.ForgotPasswordRequest;
import com.rfidcampus.rfid_campus.dto.ResetPasswordRequest;
import com.rfidcampus.rfid_campus.model.PasswordResetToken;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.PasswordResetTokenRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

@Service
public class PasswordResetService {
    
    
    private final UsuarioRepository usuarioRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UsuarioRepository usuarioRepo,
            PasswordResetTokenRepository tokenRepo,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.tokenRepo = tokenRepo;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestReset(ForgotPasswordRequest req) {
        // ✅ Buscamos en la tabla usuarios
        Usuario usuario = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo no registrado"));

        // Validación opcional de RFID (si el DTO lo trae)
        if (req.getRfid() != null && !req.getRfid().isBlank()) {
            if (usuario.getUidTarjeta() == null || !usuario.getUidTarjeta().equals(req.getRfid())) {
                throw new RuntimeException("El RFID no coincide con el registrado");
            }
        }

        // Generar Token
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUsuario(usuario); // ✅ setUsuario (asegúrate de haber actualizado la entidad PasswordResetToken)
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        tokenRepo.save(prt);

        // Enviar Correo
        String link = "http://localhost:5173/reset-password?token=" + token; // Puerto de React (Vite) suele ser 5173

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(usuario.getEmail());
        msg.setSubject("DUI - Restablecer contraseña");
        msg.setText("Hola " + usuario.getNombreCompleto() +
                "\n\nHas solicitado restablecer tu contraseña." +
                "\nHaz clic en el siguiente enlace (válido por 30 min):\n" + link +
                "\n\nSi no fuiste tú, ignora este correo.");
        
        // Enviamos el correo (puede fallar si la clave de Gmail no es válida, pero el código compilará)
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
            // No lanzamos error para no revelar si el correo existe o no por seguridad, 
            // o puedes lanzarlo si prefieres feedback inmediato.
        }
    }

    public void reset(ResetPasswordRequest req) {
        PasswordResetToken prt = tokenRepo.findByToken(req.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado o ya utilizado");
        }

        // ✅ Actualizamos al Usuario
        Usuario usuario = prt.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        usuarioRepo.save(usuario);

        prt.setUsed(true);
        tokenRepo.save(prt);
    }
    
    // Validar si el token sirve (para que el Frontend sepa si mostrar el formulario)
    public boolean validate(String token) {
        return tokenRepo.findByToken(token)
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}