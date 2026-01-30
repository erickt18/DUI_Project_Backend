package com.rfidcampus.rfid_campus.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envía un correo de recuperación de contraseña
     * @param toEmail Email del destinatario
     * @param token Token de recuperación
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("erickt2006.git@gmail.com"); // ⚠️ CAMBIA ESTO por tu email
            helper.setTo(toEmail);
            helper.setSubject("🔐 Recuperación de Contraseña - RFID Campus");

            // 🔗 Link con el token incluido
            String resetLink = "http://localhost:5500/reset-password.html?token=" + token;

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f5f5;">
                    <div style="background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <h2 style="color: #667eea; text-align: center;">🔐 Recuperación de Contraseña</h2>
                        <p style="color: #333; font-size: 16px;">Hola,</p>
                        <p style="color: #666; line-height: 1.6;">
                            Recibimos una solicitud para restablecer tu contraseña. Haz clic en el botón de abajo para crear una nueva:
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 14px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">
                                Restablecer Contraseña
                            </a>
                        </div>
                        <p style="color: #999; font-size: 13px; text-align: center;">
                            ⏰ Este enlace expira en 1 hora
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px; text-align: center;">
                            Si no solicitaste este cambio, ignora este correo.<br>
                            Tu contraseña no será modificada.
                        </p>
                        <p style="color: #bbb; font-size: 11px; text-align: center; margin-top: 15px;">
                            Sistema RFID Campus © 2026
                        </p>
                    </div>
                </div>
                """, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("✅ Email de recuperación enviado a: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo enviar el email de recuperación");
        }
    }
}
