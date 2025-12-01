// service/PasswordResetService.java
package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.dto.ForgotPasswordRequest;
import com.rfidcampus.rfid_campus.dto.ResetPasswordRequest;
import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.PasswordResetToken;
import com.rfidcampus.rfid_campus.repository.EstudianteRepository;
import com.rfidcampus.rfid_campus.repository.PasswordResetTokenRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final EstudianteRepository estudianteRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            EstudianteRepository estudianteRepo,
            PasswordResetTokenRepository tokenRepo,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {
        this.estudianteRepo = estudianteRepo;
        this.tokenRepo = tokenRepo;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestReset(ForgotPasswordRequest req) {
        Estudiante est = estudianteRepo.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Correo no registrado"));

        if (req.rfid() != null && !req.rfid().isBlank()) {
            if (est.getUidTarjeta() == null || !est.getUidTarjeta().equals(req.rfid())) {
                throw new RuntimeException("El RFID no coincide con el registrado");
            }
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setEstudiante(est);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        tokenRepo.save(prt);

        // URL del FRONTEND que hará el reset (mándala en el mail)
        String link = "http://localhost:5000/reset?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(est.getEmail());
        msg.setSubject("DUI - Restablecer contraseña");
        msg.setText("Hola " + est.getNombreCompleto() +
                "\n\nHas solicitado restablecer tu contraseña." +
                "\nHaz clic en el siguiente enlace (válido por 30 min):\n" + link +
                "\n\nSi no fuiste tú, ignora este correo.");
        mailSender.send(msg);
    }

    public void reset(ResetPasswordRequest req) {
        PasswordResetToken prt = tokenRepo.findByToken(req.token())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado o ya utilizado");
        }

        Estudiante est = prt.getEstudiante();
        est.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        estudianteRepo.save(est);

        prt.setUsed(true);
        tokenRepo.save(prt);
    }

    public boolean validate(String token) {
        return tokenRepo.findByToken(token)
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}
