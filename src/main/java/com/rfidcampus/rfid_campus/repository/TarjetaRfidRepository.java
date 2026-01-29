package com.rfidcampus.rfid_campus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface TarjetaRfidRepository extends JpaRepository<TarjetaRfid, String> {
   
    Optional<TarjetaRfid> findByUsuario(Usuario usuario);
    
    
    Optional<TarjetaRfid> findByTarjetaUid(String tarjetaUid);
}
