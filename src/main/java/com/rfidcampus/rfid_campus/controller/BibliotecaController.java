package com.rfidcampus.rfid_campus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.dto.LibroUpdateRequest;
import com.rfidcampus.rfid_campus.model.Libro;
import com.rfidcampus.rfid_campus.service.BibliotecaService;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    // ================= CRUD LIBROS (COMPLETO) =================

    // 1. LEER / BUSCAR (MODIFICADO) ✅
    @GetMapping("/libros")
    public ResponseEntity<List<Libro>> listarLibros(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String tipo // <--- ESTO FALTABA
    ) {
        // 1. Si piden tipo (TESIS, ARTICULO...), filtramos
        if (tipo != null && !tipo.isBlank()) {
            return ResponseEntity.ok(bibliotecaService.buscarPorTipo(tipo));
        }

        // 2. Si piden búsqueda por título
        if (busqueda != null && !busqueda.isBlank()) {
            return ResponseEntity.ok(bibliotecaService.buscarPorTitulo(busqueda));
        }

        // 3. Si no piden nada, devolvemos todo
        return ResponseEntity.ok(bibliotecaService.listarTodosLibros());
    }

    // 2. CREAR (Agregar)
    @PostMapping("/libros")
    public ResponseEntity<Libro> crearLibro(@RequestBody Libro libro) {
        return ResponseEntity.ok(bibliotecaService.guardarLibro(libro));
    }

    // 3. EDITAR (Actualizar)
    @PutMapping("/libros/{id}")
    public ResponseEntity<?> actualizarLibro(@PathVariable Long id, @RequestBody LibroUpdateRequest req) {
        try {
            return ResponseEntity.ok(bibliotecaService.actualizarLibro(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. ELIMINAR 
    @DeleteMapping("/libros/{id}")
    public ResponseEntity<?> eliminarLibro(@PathVariable Long id) {
        try {
            bibliotecaService.eliminarLibro(id);
            return ResponseEntity.ok(Map.of("message", "Libro eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar (quizás tiene préstamos activos)"));
        }
    }

    // ================= PRÉSTAMOS (Logica existente) =================
    @PostMapping("/prestamos")
    public ResponseEntity<?> registrarPrestamo(@RequestParam String uid, @RequestParam Long idLibro, @RequestParam(required = false) Integer dias) {
        try {
            var registro = bibliotecaService.registrarPrestamo(uid, idLibro, dias);
            return ResponseEntity.ok(Map.of("mensaje", "Préstamo exitoso", "libro", registro.getLibro().getTitulo()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/devoluciones/{id}")
    public ResponseEntity<?> registrarDevolucion(@PathVariable Long id) {
        try {
            bibliotecaService.registrarDevolucion(id);
            return ResponseEntity.ok(Map.of("mensaje", "Libro devuelto correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ================= ESTUDIANTE Y COLA =================
    
    @GetMapping("/mis-prestamos")
    public ResponseEntity<?> verMisPrestamos(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(bibliotecaService.obtenerPrestamosPorEmail(email));
    }

    @PostMapping("/libros/{id}/espera")
    public ResponseEntity<?> unirseACola(@PathVariable Long id, @RequestParam String email) {
        return ResponseEntity.ok(Map.of("mensaje", bibliotecaService.agregarAColaEspera(id, email)));
    }
}