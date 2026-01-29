package com.rfidcampus.rfid_campus.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.model.Usuario;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<Transaccion> findTop100ByOrderByFechaDesc();
    List<Transaccion> findByUsuarioEmailAndTipoOrderByFechaDesc(String email, String tipo);
    List<Transaccion> findByUsuarioOrderByFechaDesc(Usuario usuario);

    
    
    // 1. Obtener transacciones de un mes específico
    @Query("SELECT t FROM Transaccion t WHERE YEAR(t.fecha) = :anio AND MONTH(t.fecha) = :mes AND t.tipo = 'COMPRA_BAR'")
    List<Transaccion> findTransaccionesPorMes(@Param("mes") int mes, @Param("anio") int anio);
    
    // 2. Contar clientes únicos del mes
    @Query("SELECT COUNT(DISTINCT t.usuario.id) FROM Transaccion t WHERE YEAR(t.fecha) = :anio AND MONTH(t.fecha) = :mes AND t.tipo = 'COMPRA_BAR'")
    Long countClientesUnicosPorMes(@Param("mes") int mes, @Param("anio") int anio);
    
    // 3. Obtener transacciones por rango de fechas (para actividad por hora)
    List<Transaccion> findByFechaBetweenAndTipo(LocalDateTime inicio, LocalDateTime fin, String tipo);
}