package com.rfidcampus.rfid_campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.Transaccion;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // ✅ CORREGIDO: findByUsuarioId (antes findByEstudianteId)
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(Long id);

    List<Transaccion> findTop100ByOrderByFechaDesc();
    
    // ✅ CORREGIDO: findByUsuarioEmail (antes findByEstudianteEmail)
    List<Transaccion> findByUsuarioEmailAndTipoOrderByFechaDesc(String email, String tipo);
}