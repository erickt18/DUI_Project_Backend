package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByDisponible(Boolean disponible);
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
    List<Libro> findByAutorContainingIgnoreCase(String autor);
    List<Libro> findByCategoria(String categoria);
}