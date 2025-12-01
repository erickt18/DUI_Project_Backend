package com.rfidcampus.rfid_campus.dto;

import lombok.Data;
import java.util.List;

// DTO para recibir la lista de productos y el UID de la tarjeta
@Data
public class CompraMultipleRequest {
    private String tarjetaUid;           // UID de la tarjeta RFID
    private List<Long> productoIds;      // IDs de los productos seleccionados
}
