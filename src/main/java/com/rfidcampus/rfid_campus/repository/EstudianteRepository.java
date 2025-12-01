package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByEmail(String email);

    Optional<Estudiante> findByUidTarjeta(String uidTarjeta);

    @Query("SELECT SUM(e.saldo) FROM Estudiante e")
    Double sumSaldo();

}
