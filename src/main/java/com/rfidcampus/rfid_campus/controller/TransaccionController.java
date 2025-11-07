package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionRepository transaccionRepo;

    public TransaccionController(TransaccionRepository transaccionRepo) {
        this.transaccionRepo = transaccionRepo;
    }

    // El estudiante solo puede ver su propio historial.
    // El admin puede ver el de cualquiera.
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    @GetMapping("/estudiante/{id}")
    public List<Transaccion> historial(@PathVariable Long id, Authentication auth) {
        // Si es estudiante, solo puede ver sus propias transacciones
        // Puedes buscar el id del usuario por el Authentication y comparar con {id}
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_STUDENT"))) {
            // Aquí deberías comprobar que el id de auth coincide con el id solicitado
            // Por simplicidad, deberías tener un método que obtenga el id del estudiante por auth
            // Si no coincide, lanza excepción o retorna vacío
        }
        return transaccionRepo.findByEstudianteIdOrderByFechaDesc(id);
    }

    // Listar todas las transacciones (solo admin)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listar")
    public List<Transaccion> listarTodas() {
        return transaccionRepo.findAll();
    }
}
