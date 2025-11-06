package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroBibliotecaRepository extends JpaRepository<RegistroBiblioteca, Long> {
    List<RegistroBiblioteca> findByEstudianteIdOrderByFechaPrestamoDesc(Long estudianteId);
    List<RegistroBiblioteca> findByEstadoOrderByFechaPrestamoDesc(String estado);
    List<RegistroBiblioteca> findByEstudianteIdAndEstado(Long estudianteId, String estado);
}