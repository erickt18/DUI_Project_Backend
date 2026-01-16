package com.rfidcampus.rfid_campus.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column; // ✅ Importante para el dinero
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transacciones")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ CORRECCIÓN 1: Relación con Usuario (ya no Estudiante)
    @ManyToOne
    @JoinColumn(name = "id_usuario_fk", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 40)
    private String tipo; // Ej: "COMPRA_BAR", "RECARGA", "MULTA"

    // ✅ CORRECCIÓN 2: Uso de BigDecimal para precisión monetaria
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(length = 180)
    private String detalle; // Ej: "Coca Cola + Sanduche"

    @Column(nullable = false)
    private LocalDateTime fecha;

    // Asegura que siempre haya fecha al guardar
    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}