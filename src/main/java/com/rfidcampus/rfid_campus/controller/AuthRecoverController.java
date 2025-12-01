// controller/AuthRecoverController.java
package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.ForgotPasswordRequest;
import com.rfidcampus.rfid_campus.dto.ResetPasswordRequest;
import com.rfidcampus.rfid_campus.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRecoverController {
  private final PasswordResetService service;

  public AuthRecoverController(PasswordResetService service) {
    this.service = service;
  }
@PostMapping("/forgot-password")
public ResponseEntity<?> forgot(@RequestBody ForgotPasswordRequest req) {
    service.requestReset(req);
    return ResponseEntity.ok(Map.of("message", "Correo enviado"));
}


  @GetMapping("/reset-password/validate")
  public ResponseEntity<?> validate(@RequestParam String token) {
    return service.validate(token)
        ? ResponseEntity.ok(Map.of("valid", true))
        : ResponseEntity.badRequest().body(Map.of("valid", false));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> reset(@RequestBody ResetPasswordRequest req) {
    service.reset(req);
    return ResponseEntity.ok(Map.of("message","Contraseña actualizada"));
  }
}
