package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.dto.DashboardDTO;
import com.rfidcampus.rfid_campus.repository.EstudianteRepository;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import com.rfidcampus.rfid_campus.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EstudianteRepository estudianteRepository;
    private final TransaccionRepository transaccionRepository;
    private final ProductoRepository productoRepository;

    public DashboardDTO getDashboardData() {
        long totalEstudiantes = estudianteRepository.count();
        long totalTransacciones = transaccionRepository.count();
        Double saldoTotal = estudianteRepository.sumSaldo();
        long totalProductos = productoRepository.count();

        return new DashboardDTO(
                totalEstudiantes,
                totalTransacciones,
                saldoTotal != null ? saldoTotal : 0.0,

                totalProductos);
    }
}
