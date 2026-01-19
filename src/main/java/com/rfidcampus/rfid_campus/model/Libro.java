package com.rfidcampus.rfid_campus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro") // Importante para mapear a id_libro de la BD
    private Long id; // Puedes llamarlo 'id' o 'idLibro', pero el @Column manda

    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private Integer anio;
    
    @Column(name = "tipo_material") // Mapeo exacto snake_case
    private String tipoMaterial;
    
    private String categoria;
    
    private Integer stock; // Stock actual

    @Column(name = "stock_total") // ✅ AGREGADO (Faltaba este)
    private Integer stockTotal;

    @Column(name = "stock_disponible") // ✅ AGREGADO (Faltaba este)
    private Integer stockDisponible;
    
    private String ubicacion;
    
    private Boolean disponible;
}