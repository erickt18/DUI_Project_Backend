package com.rfidcampus.rfid_campus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    // Buscar usuario por el UID de su tarjeta RFID (Usando la relación inversa si la agregas, o JOIN)
    // Por ahora, buscaremos el UID en la tabla de tarjetas, así que este método puede esperar.
    
    boolean existsByEmail(String email);
    
}