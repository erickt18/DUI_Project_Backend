package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.RegistroAsistencia;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.repository.EstudianteRepository;
import com.rfidcampus.rfid_campus.repository.RegistroAsistenciaRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AsistenciaService {

    private final EstudianteRepository estudianteRepo;
    private final TarjetaRfidRepository tarjetaRepo;
    private final RegistroAsistenciaRepository asistenciaRepo;

    public AsistenciaService(EstudianteRepository estudianteRepo,
                             TarjetaRfidRepository tarjetaRepo,
                             RegistroAsistenciaRepository asistenciaRepo) {
        this.estudianteRepo = estudianteRepo;
        this.tarjetaRepo = tarjetaRepo;
        this.asistenciaRepo = asistenciaRepo;
    }

    // ✅ Registrar asistencia (entrada)
    public String registrarAsistencia(String uid, String aula) {

        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUidAndEstado(uid, "ACTIVA")
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada o INACTIVA"));

        Estudiante estudiante = tarjeta.getEstudiante();
        if (estudiante == null) {
            throw new RuntimeException("Tarjeta no asignada a ningún estudiante");
        }

        RegistroAsistencia asistencia = RegistroAsistencia.builder()
                .aula(aula)
                .estudiante(estudiante)
                .estado("PRESENTE")
                .fechaHora(LocalDateTime.now()) // ✅ Guardar fecha
                .build();

        asistenciaRepo.save(asistencia);
        return "ASISTENCIA_REGISTRADA";
    }

    // ✅ Consultar asistencias por ID estudiante
    public List<RegistroAsistencia> obtenerAsistenciaPorEstudiante(Long idEstudiante) {
        return asistenciaRepo.findByEstudianteIdOrderByFechaHoraDesc(idEstudiante);
    }

    // ✅ Consultar asistencias por aula
    public List<RegistroAsistencia> obtenerAsistenciaPorAula(String aula) {
        return asistenciaRepo.findByAulaOrderByFechaHoraDesc(aula);
    }

    // ✅ Consultar asistencias por UID tarjeta
    public List<RegistroAsistencia> obtenerAsistenciaPorUid(String uid) {
        return asistenciaRepo.findByEstudiante_UidTarjetaOrderByFechaHoraDesc(uid);
    }
}
