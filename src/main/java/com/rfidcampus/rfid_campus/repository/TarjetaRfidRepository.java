package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarjetaRfidRepository extends JpaRepository<TarjetaRfid, String> {

    Optional<TarjetaRfid> findByTarjetaUid(String tarjetaUid);

    Optional<TarjetaRfid> findByTarjetaUidAndEstado(String tarjetaUid, String estado);

    Optional<TarjetaRfid> findByEstudiante(Estudiante estudiante);
}
