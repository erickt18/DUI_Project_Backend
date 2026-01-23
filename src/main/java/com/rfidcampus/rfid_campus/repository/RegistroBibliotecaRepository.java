package com.rfidcampus.rfid_campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface RegistroBibliotecaRepository extends JpaRepository<RegistroBiblioteca, Long> {
    
    // Buscar historial completo
    List<RegistroBiblioteca> findByUsuario(Usuario usuario);
    
    // ✅ NUEVO: Buscar SOLO lo que debe (Pendientes)
    // Busca por usuario Y donde el estado sea "PRESTADO"
    List<RegistroBiblioteca> findByUsuarioAndEstado(Usuario usuario, String estado);
}