package com.rfidcampus.rfid_campus.controller;

import com.rfidcampus.rfid_campus.dto.PrestamoRequest;
import com.rfidcampus.rfid_campus.dto.LibroUpdateRequest;
import com.rfidcampus.rfid_campus.model.Libro;
import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.service.BibliotecaService;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import com.rfidcampus.rfid_campus.dto.PrestamoDTO;


@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    // Registrar préstamo con RFID
    @PostMapping("/prestamo")
    public ResponseEntity<?> registrarPrestamo(@RequestBody PrestamoRequest req) {
        try {
            RegistroBiblioteca registro = bibliotecaService.registrarPrestamo(
                    req.getUidTarjeta(),
                    req.getIdLibro(),
                    req.getDiasPrestamo());
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Préstamo registrado exitosamente",
                    "estudiante", registro.getEstudiante().getNombreCompleto(),
                    "libro", registro.getLibro().getTitulo(),
                    "fechaDevolucion", registro.getFechaDevolucionEstimada()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Registrar devolución
    @PutMapping("/devolucion/{idPrestamo}")
    public ResponseEntity<?> registrarDevolucion(@PathVariable Long idPrestamo) {
        try {
            RegistroBiblioteca registro = bibliotecaService.registrarDevolucion(idPrestamo);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Devolución registrada exitosamente",
                    "libro", registro.getLibro().getTitulo(),
                    "fechaDevolucion", registro.getFechaDevolucionReal()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Préstamos activos
    @GetMapping("/prestamos/activos/{idEstudiante}")
    public ResponseEntity<List<RegistroBiblioteca>> prestamosActivos(@PathVariable Long idEstudiante) {
        return ResponseEntity.ok(bibliotecaService.obtenerPrestamosActivos(idEstudiante));
    }

    // Historial por estudiante
    @GetMapping("/prestamos/historial/{idEstudiante}")
    public ResponseEntity<List<RegistroBiblioteca>> historial(@PathVariable Long idEstudiante) {
        return ResponseEntity.ok(bibliotecaService.obtenerHistorial(idEstudiante));
    }

    // Libros disponibles
    @GetMapping("/catalogo/disponibles")
    public ResponseEntity<List<Libro>> catalogoDisponibles() {
        return ResponseEntity.ok(bibliotecaService.listarLibrosDisponibles());
    }

    @GetMapping("/catalogo/buscar")
    public ResponseEntity<List<Libro>> buscarLibros(@RequestParam String titulo) {
        return ResponseEntity.ok(bibliotecaService.buscarPorTitulo(titulo));
    }

    // Agregar libro
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/catalogo/agregar")
    public ResponseEntity<?> agregarLibro(@RequestBody Libro libro) {
        try {
            Libro saved = bibliotecaService.guardarLibro(libro);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Libro agregado al catálogo",
                    "libro", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Editar libro
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/catalogo/editar/{id}")
    public ResponseEntity<?> editarLibro(@PathVariable Long id, @RequestBody LibroUpdateRequest req) {
        try {
            Libro actualizado = bibliotecaService.actualizarLibro(id, req);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Libro actualizado correctamente",
                    "libro", actualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar libro
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/catalogo/eliminar/{id}")
    public ResponseEntity<?> eliminarLibro(@PathVariable Long id) {
        try {
            bibliotecaService.eliminarLibro(id);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Libro eliminado correctamente",
                    "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Listar todos los préstamos
    @GetMapping("/prestamos")
    public ResponseEntity<List<Map<String, Object>>> listarPrestamos() {
        List<RegistroBiblioteca> registros = bibliotecaService.listarTodosPrestamos();

        List<Map<String, Object>> resultado = registros.stream().map(r -> Map.<String, Object>of(
                "id", r.getId(),
                "tituloLibro", r.getLibro().getTitulo(),
                "idLibro", r.getLibro().getId(),
                "nombreEstudiante", r.getEstudiante().getNombreCompleto(),
                "idEstudiante", r.getEstudiante().getId(),
                "fechaPrestamo", r.getFechaPrestamo(),
                "dias", ChronoUnit.DAYS.between(r.getFechaPrestamo(), r.getFechaDevolucionEstimada()),
                "estado", r.getEstado())).toList();

        return ResponseEntity.ok(resultado);
    }

    // Listar devoluciones
    @GetMapping("/devoluciones")
    public ResponseEntity<List<Map<String, Object>>> listarDevoluciones() {
        List<RegistroBiblioteca> registros = bibliotecaService.listarDevoluciones();

        List<Map<String, Object>> resultado = registros.stream().map(r -> Map.<String, Object>of(
                "idPrestamo", r.getId(),
                "tituloLibro", r.getLibro().getTitulo(),
                "nombreEstudiante", r.getEstudiante().getNombreCompleto(),
                "fechaDevolucionReal", r.getFechaDevolucionReal())).toList();

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/catalogo/todos")
    public ResponseEntity<List<Libro>> listarTodos() {
        return ResponseEntity.ok(bibliotecaService.listarTodosLibros());
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/mis-prestamos")
    public ResponseEntity<List<PrestamoDTO>> misPrestamos(Authentication auth) {
        String email = auth.getName();
        List<PrestamoDTO> prestamos = bibliotecaService.obtenerPrestamosPorEmail(email);
        return ResponseEntity.ok(prestamos);
    }

}
