package com.rfidcampus.rfid_campus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface TarjetaRfidRepository extends JpaRepository<TarjetaRfid, String> {
    // 👇 AGREGA ESTA LÍNEA PARA QUE FUNCIONE EL BLOQUEO POR EMAIL
    Optional<TarjetaRfid> findByUsuario(Usuario usuario);
}