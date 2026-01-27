package com.rfidcampus.rfid_campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    
    // Busca transacciones filtrando por el ID del usuario
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    
    // Trae las últimas 100 de todo el sistema (para el Admin del Bar quizás)
    List<Transaccion> findTop100ByOrderByFechaDesc();

 
    // Busca por el email del usuario y el tipo de movimiento (ej: "COMPRA_BAR")
    List<Transaccion> findByUsuarioEmailAndTipoOrderByFechaDesc(String email, String tipo);

   
    // Busca pasando el OBJETO usuario completo
    List<Transaccion> findByUsuarioOrderByFechaDesc(Usuario usuario);
}