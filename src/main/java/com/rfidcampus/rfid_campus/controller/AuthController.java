package com.rfidcampus.rfid_campus.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.dto.ForgotPasswordRequest;
import com.rfidcampus.rfid_campus.dto.LoginRequest;
import com.rfidcampus.rfid_campus.dto.RegisterRequest;
import com.rfidcampus.rfid_campus.dto.ResetPasswordRequest;
import com.rfidcampus.rfid_campus.model.Rol;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.RolRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;
import com.rfidcampus.rfid_campus.security.JwtService;
import com.rfidcampus.rfid_campus.service.PasswordResetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetService passwordResetService;

    // ================= REGISTRO =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
        }

        String nombreRol = request.getRol() != null ? request.getRol() : "ROLE_ESTUDIANTE";
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));

        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .saldo(BigDecimal.ZERO)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente"));
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(usuario.getEmail(), Map.of("rol", usuario.getRolNombre()));
        return ResponseEntity.ok(Map.of("token", jwtToken));
    }

    // ================= RECUPERAR CONTRASEÑA (GMAIL) =================
    
    // 1. Solicitar el correo (Envía el link)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            System.out.println("📧 Email recibido: " + req.getEmail());
            
            if (req.getEmail() == null || req.getEmail().isEmpty()) {
                System.err.println("❌ Email vacío o null");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El email es requerido"));
            }
            
            passwordResetService.requestReset(req);
            System.out.println("✅ Email de recuperación enviado");
            return ResponseEntity.ok(Map.of("message", "Correo de recuperación enviado."));
        } catch (RuntimeException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 2. Validar token
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.isTokenValid(token); // ✅ CORREGIDO
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    // 3. Cambiar la contraseña finalmente
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            passwordResetService.resetPassword(req); // ✅ CORREGIDO
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
