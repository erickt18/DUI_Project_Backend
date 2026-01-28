package com.rfidcampus.rfid_campus.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMensualDTO {
    
    // Totales generales
    private BigDecimal ventasTotales;
    private Long totalTransacciones;
    private Long clientesUnicos;
    
    // Para gráficos
    private List<VentaDiariaDTO> ventasPorDia;  // Gráfico de líneas
    private List<ProductoVendidoDTO> productosMasVendidos;  // Gráfico de barras
    private Map<String, Integer> transaccionesPorHora;  // Gráfico de actividad
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VentaDiariaDTO {
        private Integer dia;
        private BigDecimal total;
        private Long cantidad;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductoVendidoDTO {
        private String nombreProducto;
        private Long cantidad;
        private BigDecimal totalVendido;
    }
}