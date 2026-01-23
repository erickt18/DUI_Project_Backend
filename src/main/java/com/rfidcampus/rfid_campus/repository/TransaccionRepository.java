package com.rfidcampus.rfid_campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // 1. Usado en: obtenerHistorialPorUsuario(Long idUsuario)
    // Busca transacciones filtrando por el ID del usuario
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // 2. Usado en: obtenerUltimasTransacciones()
    // Trae las últimas 100 de todo el sistema (para el Admin del Bar quizás)
    List<Transaccion> findTop100ByOrderByFechaDesc();

    // 3. Usado en: buscarPorEmailYTipo()
    // Busca por el email del usuario y el tipo de movimiento (ej: "COMPRA_BAR")
    List<Transaccion> findByUsuarioEmailAndTipoOrderByFechaDesc(String email, String tipo);

    // 4. (Opcional) El que usamos en el servicio de Estudiante antes
    // Busca pasando el OBJETO usuario completo
    List<Transaccion> findByUsuarioOrderByFechaDesc(Usuario usuario);
}