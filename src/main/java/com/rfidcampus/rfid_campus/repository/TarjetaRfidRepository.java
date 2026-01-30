package com.rfidcampus.rfid_campus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface TarjetaRfidRepository extends JpaRepository<TarjetaRfid, String> {
    
    Optional<TarjetaRfid> findByUsuario(Usuario usuario);
    
     @Query("SELECT t FROM TarjetaRfid t WHERE t.usuario.id = :usuarioId")
    Optional<TarjetaRfid> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
