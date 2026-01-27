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
    @Column(name = "id_libro") 
    private Long id; 

    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private Integer anio;
    
    @Column(name = "tipo_material") 
    private String tipoMaterial;
    
    private String categoria;
    
    private Integer stock; 

    @Column(name = "stock_total") 
    private Integer stockTotal;

    @Column(name = "stock_disponible") 
    private Integer stockDisponible;
    
    private String ubicacion;
    
    private Boolean disponible;
}