package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroBibliotecaRepository extends JpaRepository<RegistroBiblioteca, Long> {

    // Historial completo del estudiante (ordenado por fecha préstamo)
    List<RegistroBiblioteca> findByEstudianteIdOrderByFechaPrestamoDesc(Long estudianteId);

    // Préstamos activos del estudiante
    List<RegistroBiblioteca> findByEstudianteIdAndEstado(Long estudianteId, String estado);

    // ✅ Buscar devoluciones y ordenarlas por fecha real de devolución
    List<RegistroBiblioteca> findByEstadoOrderByFechaDevolucionRealDesc(String estado);

    List<RegistroBiblioteca> findByEstudiante(Estudiante estudiante);

}
