package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.PrestamoRequest;
import com.rfidcampus.rfid_campus.model.Libro;
import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.service.BibliotecaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    // ✅ Registrar préstamo con RFID
    @PostMapping("/prestamo")
    public ResponseEntity<?> registrarPrestamo(@RequestBody PrestamoRequest req) {
        try {
            RegistroBiblioteca registro = bibliotecaService.registrarPrestamo(
                    req.getUidTarjeta(),
                    req.getIdLibro(),
                    req.getDiasPrestamo()
            );
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Préstamo registrado exitosamente",
                    "estudiante", registro.getEstudiante().getNombreCompleto(),
                    "libro", registro.getLibro().getTitulo(),
                    "fechaDevolucion", registro.getFechaDevolucionEstimada()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Registrar devolución
    @PutMapping("/devolucion/{idPrestamo}")
    public ResponseEntity<?> registrarDevolucion(@PathVariable Long idPrestamo) {
        try {
            RegistroBiblioteca registro = bibliotecaService.registrarDevolucion(idPrestamo);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Devolución registrada exitosamente",
                    "libro", registro.getLibro().getTitulo(),
                    "fechaDevolucion", registro.getFechaDevolucionReal()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Ver préstamos activos de un estudiante
    @GetMapping("/prestamos/activos/{idEstudiante}")
    public ResponseEntity<List<RegistroBiblioteca>> prestamosActivos(@PathVariable Long idEstudiante) {
        return ResponseEntity.ok(bibliotecaService.obtenerPrestamosActivos(idEstudiante));
    }

    // ✅ Historial completo de préstamos
    @GetMapping("/prestamos/historial/{idEstudiante}")
    public ResponseEntity<List<RegistroBiblioteca>> historial(@PathVariable Long idEstudiante) {
        return ResponseEntity.ok(bibliotecaService.obtenerHistorial(idEstudiante));
    }

    // ✅ Catálogo de libros disponibles
    @GetMapping("/catalogo/disponibles")
    public ResponseEntity<List<Libro>> catalogoDisponibles() {
        return ResponseEntity.ok(bibliotecaService.listarLibrosDisponibles());
    }

    // ✅ Buscar libros por título
    @GetMapping("/catalogo/buscar")
    public ResponseEntity<List<Libro>> buscarLibros(@RequestParam String titulo) {
        return ResponseEntity.ok(bibliotecaService.buscarPorTitulo(titulo));
    }

    // ✅ Agregar libro al catálogo
    @PostMapping("/catalogo/agregar")
    public ResponseEntity<?> agregarLibro(@RequestBody Libro libro) {
        try {
            Libro saved = bibliotecaService.guardarLibro(libro);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Libro agregado al catálogo",
                    "libro", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}