package com.rfidcampus.rfid_campus.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quién hizo la transacción
    @ManyToOne
    @JoinColumn(name = "id_estudiante_fk", referencedColumnName = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    // RECARGA | COMPRA_PRODUCTO | COMPRA_BAR | ...
    @Column(nullable = false, length = 40)
    private String tipo;

    // Monto (positivo para recarga, negativo si así lo decides para compras — por ahora positivo)
    @Column(nullable = false)
    private Double monto;

    // 👇 Nuevo: nombre del producto o descripción libre
    @Column(length = 180)
    private String detalle;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
