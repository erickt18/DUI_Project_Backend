package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.RegistroAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistencia, Long> {

    // Historial de un estudiante
    List<RegistroAsistencia> findByEstudianteIdOrderByFechaHoraDesc(Long idEstudiante);

    // Filtrar por aula
    List<RegistroAsistencia> findByAulaOrderByFechaHoraDesc(String aula);

    // Buscar por UID de tarjeta RFID
    List<RegistroAsistencia> findByEstudiante_UidTarjetaOrderByFechaHoraDesc(String uid);
}
