package com.rfidcampus.rfid_campus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty("id_libro")
    private Long id; 

    @JsonProperty("titulo")
    private String titulo;
    
    @JsonProperty("autor")
    private String autor;
    
    @JsonProperty("isbn")
    private String isbn;
    
    @JsonProperty("editorial")
    private String editorial;
    
    @JsonProperty("anio")
    private Integer anio;
    
    @Column(name = "tipo_material") 
    @JsonProperty("tipo_material")
    private String tipoMaterial;
    
    @JsonProperty("categoria")
    private String categoria;
    
    @JsonProperty("stock")
    private Integer stock; 

    @Column(name = "stock_total") 
    @JsonProperty("stock_total")
    private Integer stockTotal;

    @Column(name = "stock_disponible") 
    @JsonProperty("stock_disponible")
    private Integer stockDisponible;
    
    @JsonProperty("ubicacion")
    private String ubicacion;
    
    @JsonProperty("disponible")
    private Boolean disponible;
}
