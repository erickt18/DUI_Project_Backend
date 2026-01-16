package com.rfidcampus.rfid_campus.controller;

import java.math.BigDecimal; // Asegúrate de tener este DTO o usa Map
import java.util.Map; // Asegúrate de tener este DTO

import org.springframework.http.ResponseEntity; // Asegúrate de tener este DTO
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // ✅ Antes Estudiante
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping; // ✅ Antes EstudianteRepository
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.dto.LoginRequest;
import com.rfidcampus.rfid_campus.dto.RegisterRequest;
import com.rfidcampus.rfid_campus.model.Rol;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.RolRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;
import com.rfidcampus.rfid_campus.security.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository; // ✅ Corregido
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
        }

        // Buscar Rol (Por defecto ESTUDIANTE si no se envía)
        String nombreRol = request.getRol() != null ? request.getRol() : "ROLE_ESTUDIANTE";
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));

        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .saldo(BigDecimal.ZERO) // ✅ BigDecimal
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // Generar Token
        // Nota: Asegúrate que tu JwtService soporte generar token con los claims extras si los usas
        String jwtToken = jwtService.generateToken(usuario.getEmail(), Map.of("rol", usuario.getRolNombre()));

        return ResponseEntity.ok(Map.of("token", jwtToken));
    }
}