package com.rfidcampus.rfid_campus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository; // ✅ Antes importaba Estudiante

import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface TarjetaRfidRepository extends JpaRepository<TarjetaRfid, String> {

    Optional<TarjetaRfid> findByTarjetaUid(String tarjetaUid);

    Optional<TarjetaRfid> findByTarjetaUidAndEstado(String tarjetaUid, String estado);

    // ✅ CORREGIDO: findByUsuario (antes findByEstudiante)
    Optional<TarjetaRfid> findByUsuario(Usuario usuario);
}