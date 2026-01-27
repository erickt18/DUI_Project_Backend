package com.rfidcampus.rfid_campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.Libro;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    // 1. Para el buscador general (Por título)
    List<Libro> findByTituloContainingIgnoreCase(String titulo);

    // 2. Para filtrar Tesis vs Libros
    List<Libro> findByTipoMaterialIgnoreCase(String tipoMaterial);

    
    // Sirve para encontrar solo los libros que tienen stock (disponible = true)
    List<Libro> findByDisponible(Boolean disponible);
}