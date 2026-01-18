package com.rfidcampus.rfid_campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.Transaccion;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // ✅ Debe decir UsuarioId, no EstudianteId
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(Long id);

    List<Transaccion> findTop100ByOrderByFechaDesc();
    
    // ✅ Debe decir UsuarioEmail
    List<Transaccion> findByUsuarioEmailAndTipoOrderByFechaDesc(String email, String tipo);
}