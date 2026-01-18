package com.rfidcampus.rfid_campus.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.Libro;
import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.service.BibliotecaService;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    // ================= LIBROS =================
    @GetMapping("/libros")
    public ResponseEntity<List<Libro>> listarLibros() {
        // ✅ Ahora sí encontrará este método
        return ResponseEntity.ok(bibliotecaService.listarTodosLibros());
    }

    @PostMapping("/libros")
    public ResponseEntity<Libro> crearLibro(@RequestBody Libro libro) {
        return ResponseEntity.ok(bibliotecaService.guardarLibro(libro));
    }
    
    // ================= PRÉSTAMOS =================
    @PostMapping("/prestamos")
    public ResponseEntity<?> registrarPrestamo(
            @RequestParam String uid, 
            @RequestParam Long idLibro, 
            @RequestParam(required = false) Integer dias) {
        try {
            var registro = bibliotecaService.registrarPrestamo(uid, idLibro, dias);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Préstamo exitoso", 
                "libro", registro.getLibro().getTitulo(),
                "usuario", registro.getUsuario().getNombreCompleto() // ✅ CORREGIDO: getUsuario()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/devoluciones/{id}")
    public ResponseEntity<?> registrarDevolucion(@PathVariable Long id) {
        try {
            var registro = bibliotecaService.registrarDevolucion(id);
            return ResponseEntity.ok(Map.of("mensaje", "Libro devuelto correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ================= HISTORIAL Y REPORTES =================
    @GetMapping("/prestamos/activos") // Reporte de qué libros están fuera
    public ResponseEntity<List<Map<String, Object>>> listarPrestamosActivos() {
        List<RegistroBiblioteca> prestamos = bibliotecaService.listarTodosPrestamos();
        
        // Transformamos la lista para enviar un JSON limpio
        List<Map<String, Object>> respuesta = prestamos.stream()
            .map(r -> {
                // ✅ CORREGIDO: getUsuario() en lugar de getEstudiante()
                String nombreUsuario = r.getUsuario() != null ? r.getUsuario().getNombreCompleto() : "Desconocido";
                return Map.<String, Object>of(
                    "id", r.getId(),
                    "libro", r.getLibro().getTitulo(),
                    "usuario", nombreUsuario,
                    "fechaPrestamo", r.getFechaPrestamo().toString(),
                    "estado", r.getEstado()
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }
    
    // Endpoint para la COLA DE ESPERA
    @PostMapping("/libros/{id}/espera")
    public ResponseEntity<?> unirseACola(@PathVariable Long id, @RequestParam String email) {
        String mensaje = bibliotecaService.agregarAColaEspera(id, email);
        return ResponseEntity.ok(Map.of("mensaje", mensaje));
    }
}