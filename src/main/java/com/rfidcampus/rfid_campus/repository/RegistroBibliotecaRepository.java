package com.rfidcampus.rfid_campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; // ✅ Antes Estudiante

import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface RegistroBibliotecaRepository extends JpaRepository<RegistroBiblioteca, Long> {

    // ✅ CAMBIO: findByUsuarioId (antes findByEstudianteId)
    List<RegistroBiblioteca> findByUsuarioIdOrderByFechaPrestamoDesc(Long usuarioId);

    // ✅ CAMBIO: findByUsuarioIdAndEstado
    List<RegistroBiblioteca> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    List<RegistroBiblioteca> findByEstadoOrderByFechaDevolucionRealDesc(String estado);

    // ✅ CAMBIO: findByUsuario (antes findByEstudiante)
    List<RegistroBiblioteca> findByUsuario(Usuario usuario);
}