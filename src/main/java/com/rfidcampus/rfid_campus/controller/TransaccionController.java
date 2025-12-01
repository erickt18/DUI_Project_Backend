package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionRepository transaccionRepo;

    // ✅ Últimas 100 transacciones (solo admin)
    @GetMapping("/ultimas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Transaccion> ultimas() {
        return transaccionRepo.findTop100ByOrderByFechaDesc();
    }

    // ✅ Historial por estudiante
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping("/estudiante/{id}")
    public List<Transaccion> historial(@PathVariable Long id) {
        return transaccionRepo.findByEstudianteIdOrderByFechaDesc(id);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mis-compras")
    public List<Transaccion> misCompras(org.springframework.security.core.Authentication auth) {
        String email = auth.getName();
        return transaccionRepo.findByEstudianteEmailAndTipoOrderByFechaDesc(email, "COMPRA");
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mis-compras-bar")
    public List<Transaccion> misComprasBar(Authentication auth) {
        String email = auth.getName();
        // SOLO DEL BAR:
        return transaccionRepo.findByEstudianteEmailAndTipoOrderByFechaDesc(email, "COMPRA_BAR");
    }

}
