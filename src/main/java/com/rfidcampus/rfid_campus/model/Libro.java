package com.rfidcampus.rfid_campus.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "libros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 50, unique = true)
    private String isbn;

    @Column(length = 150)
    private String autor;

    @Column(length = 100)
    private String editorial;

    private Integer anio;

    @Column(name = "tipo_material", length = 50)
    private String tipoMaterial; // libro, revista, tesis

    @Column(length = 100)
    private String categoria;

    @Column(nullable = false)
    private Boolean disponible = true;

    private Integer stock;
}
