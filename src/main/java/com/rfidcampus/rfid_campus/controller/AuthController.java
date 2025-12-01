package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.AuthResponse;
import com.rfidcampus.rfid_campus.dto.LoginRequest;
import com.rfidcampus.rfid_campus.dto.RegistroRequest;
import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.Rol;
import com.rfidcampus.rfid_campus.repository.EstudianteRepository;
import com.rfidcampus.rfid_campus.repository.RolRepository;
import com.rfidcampus.rfid_campus.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder encoder;
    private final RolRepository rolRepository;

    public AuthController(AuthenticationManager authManager,
                         JwtService jwtService,
                         EstudianteRepository estudianteRepository,
                         PasswordEncoder encoder,
                         RolRepository rolRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.estudianteRepository = estudianteRepository;
        this.encoder = encoder;
        this.rolRepository = rolRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        try {
            var tokenReq = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
            authManager.authenticate(tokenReq);

            Estudiante est = estudianteRepository.findByEmail(req.getEmail()).orElseThrow();

            String roleSpring = "ROLE_" + est.getRol().getNombre().toUpperCase();
            String token = jwtService.generateToken(est.getEmail(), Map.of(
                "id", est.getId(),
                "nombre", est.getNombreCompleto(),
                "rol", est.getRol().getNombre(),
                "authorities", List.of(roleSpring)
            ));

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new AuthResponse("Credenciales incorrectas"));
        }
    }

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegistroRequest req) {
    if (estudianteRepository.findByEmail(req.getEmail()).isPresent()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Email ya registrado"));
    }
    try {
        Rol rol = rolRepository.findByNombre(req.getRolNombre())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Estudiante est = Estudiante.builder()
            .nombreCompleto(req.getNombreCompleto())
            .carrera(req.getCarrera())
            .email(req.getEmail())
            .passwordHash(encoder.encode(req.getPassword()))
            .activo(true)
            .rol(rol)
            .saldo(0.0)
            .build();
        estudianteRepository.save(est);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente"));
    } catch (Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error", "Error interno: " + ex.getMessage()));
    }
}


}
