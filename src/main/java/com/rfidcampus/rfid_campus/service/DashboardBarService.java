package com.rfidcampus.rfid_campus.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rfidcampus.rfid_campus.dto.ReporteMensualDTO;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardBarService {

    private final TransaccionRepository transaccionRepo;

    public ReporteMensualDTO generarReporteMensual(int mes, int anio) {
        
        // 1. Obtener todas las transacciones del mes
        List<Transaccion> transacciones = transaccionRepo.findTransaccionesPorMes(mes, anio);
        
        // 2. Calcular totales
        BigDecimal ventasTotales = transacciones.stream()
                .map(Transaccion::getMonto)
                .map(BigDecimal::abs)  // Convertir negativos a positivos
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalTransacciones = (long) transacciones.size();
        Long clientesUnicos = transaccionRepo.countClientesUnicosPorMes(mes, anio);
        
        // 3. Agrupar ventas por día (para gráfico de líneas)
        List<ReporteMensualDTO.VentaDiariaDTO> ventasPorDia = generarVentasPorDia(transacciones, mes, anio);
        
        // 4. Productos más vendidos (para gráfico de barras)
        List<ReporteMensualDTO.ProductoVendidoDTO> productosMasVendidos = generarProductosMasVendidos(transacciones);
        
        // 5. Actividad por hora (para gráfico de calor/actividad)
        Map<String, Integer> transaccionesPorHora = generarActividadPorHora(transacciones);
        
        return ReporteMensualDTO.builder()
                .ventasTotales(ventasTotales)
                .totalTransacciones(totalTransacciones)
                .clientesUnicos(clientesUnicos)
                .ventasPorDia(ventasPorDia)
                .productosMasVendidos(productosMasVendidos)
                .transaccionesPorHora(transaccionesPorHora)
                .build();
    }

    // ========== MÉTODOS AUXILIARES ==========
    
    private List<ReporteMensualDTO.VentaDiariaDTO> generarVentasPorDia(List<Transaccion> transacciones, int mes, int anio) {
        
        // Agrupar por día
        Map<Integer, List<Transaccion>> porDia = transacciones.stream()
                .collect(Collectors.groupingBy(t -> t.getFecha().getDayOfMonth()));
        
        List<ReporteMensualDTO.VentaDiariaDTO> resultado = new ArrayList<>();
        
        // Obtener días del mes
        YearMonth yearMonth = YearMonth.of(anio, mes);
        int diasDelMes = yearMonth.lengthOfMonth();
        
        for (int dia = 1; dia <= diasDelMes; dia++) {
            List<Transaccion> transDelDia = porDia.getOrDefault(dia, new ArrayList<>());
            
            BigDecimal totalDia = transDelDia.stream()
                    .map(Transaccion::getMonto)
                    .map(BigDecimal::abs)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            resultado.add(new ReporteMensualDTO.VentaDiariaDTO(dia, totalDia, (long) transDelDia.size()));
        }
        
        return resultado;
    }
    
    private List<ReporteMensualDTO.ProductoVendidoDTO> generarProductosMasVendidos(List<Transaccion> transacciones) {
        
        // Agrupar por producto (detalle)
        Map<String, List<Transaccion>> porProducto = transacciones.stream()
                .collect(Collectors.groupingBy(Transaccion::getDetalle));
        
        List<ReporteMensualDTO.ProductoVendidoDTO> resultado = new ArrayList<>();
        
        for (Map.Entry<String, List<Transaccion>> entry : porProducto.entrySet()) {
            String producto = entry.getKey();
            List<Transaccion> transProducto = entry.getValue();
            
            Long cantidad = (long) transProducto.size();
            BigDecimal total = transProducto.stream()
                    .map(Transaccion::getMonto)
                    .map(BigDecimal::abs)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            resultado.add(new ReporteMensualDTO.ProductoVendidoDTO(producto, cantidad, total));
        }
        
        // Ordenar por cantidad vendida (descendente)
        resultado.sort((a, b) -> b.getCantidad().compareTo(a.getCantidad()));
        
        // Retornar solo top 10
        return resultado.stream().limit(10).collect(Collectors.toList());
    }
    
    private Map<String, Integer> generarActividadPorHora(List<Transaccion> transacciones) {
        
        Map<String, Integer> actividad = new HashMap<>();
        
        // Inicializar las 24 horas
        for (int hora = 0; hora < 24; hora++) {
            actividad.put(String.format("%02d:00", hora), 0);
        }
        
        // Contar transacciones por hora
        for (Transaccion t : transacciones) {
            int hora = t.getFecha().getHour();
            String horaKey = String.format("%02d:00", hora);
            actividad.put(horaKey, actividad.get(horaKey) + 1);
        }
        
        return actividad;
    }
}