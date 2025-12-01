package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.ProductoRequest;
import com.rfidcampus.rfid_campus.dto.ProductoResponse;
import com.rfidcampus.rfid_campus.model.Producto;
import com.rfidcampus.rfid_campus.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProductoResponse create(@Valid @RequestBody ProductoRequest in) {
        return service.create(in);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProductoResponse update(@PathVariable Long id, @Valid @RequestBody ProductoRequest in) {
        return service.update(id, in);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ProductoResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public Page<ProductoResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(q, activo, page, size);
    }

    // ✅ NUEVO - PARA LISTAR PRODUCTOS EN COMPRAS
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/all")
    public List<Producto> listarProductos() {
        return service.findAll();
    }

}
