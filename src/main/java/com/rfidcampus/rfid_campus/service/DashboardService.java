package com.rfidcampus.rfid_campus.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service; 

import com.rfidcampus.rfid_campus.dto.DashboardDTO;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.ProductoRepository; 
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor; 

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsuarioRepository usuarioRepository; 
    private final TransaccionRepository transaccionRepository;
    private final ProductoRepository productoRepository;

    public DashboardDTO getDashboardData() {
        long totalUsuarios = usuarioRepository.count(); // Antes totalEstudiantes
        long totalTransacciones = transaccionRepository.count();
        long totalProductos = productoRepository.count();

        // Calcular saldo total sumando BigDecimals
        BigDecimal saldoTotal = usuarioRepository.findAll().stream()
                .map(Usuario::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardDTO(
                totalUsuarios,
                totalTransacciones,
                saldoTotal.doubleValue(), // Convertimos a double solo para el DTO visual
                totalProductos);
    }
}