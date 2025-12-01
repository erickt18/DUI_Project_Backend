package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.dto.LibroUpdateRequest;
import com.rfidcampus.rfid_campus.dto.PrestamoDTO;
import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.Libro;
import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.repository.EstudianteRepository;
import com.rfidcampus.rfid_campus.repository.LibroRepository;
import com.rfidcampus.rfid_campus.repository.RegistroBibliotecaRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BibliotecaService {

    private final LibroRepository libroRepo;
    private final RegistroBibliotecaRepository registroRepo;
    private final TarjetaRfidRepository tarjetaRepo;
    private final EstudianteRepository estudianteRepository;

    public BibliotecaService(
            LibroRepository libroRepo,
            RegistroBibliotecaRepository registroRepo,
            TarjetaRfidRepository tarjetaRepo,
            EstudianteRepository estudianteRepository) {
        this.libroRepo = libroRepo;
        this.registroRepo = registroRepo;
        this.tarjetaRepo = tarjetaRepo;
        this.estudianteRepository = estudianteRepository;
    }

    // ======================== PRÉSTAMO ========================
    @Transactional
    public RegistroBiblioteca registrarPrestamo(String uid, Long idLibro, Integer diasPrestamo) {

        // Buscar por UID sin filtrar por estado; validar estado aquí
        TarjetaRfid tarjeta = tarjetaRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if ("BLOQUEADA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("Tarjeta bloqueada. No se puede registrar el préstamo.");
        }

        Estudiante estudiante = tarjeta.getEstudiante();
        if (estudiante == null) {
            throw new RuntimeException("Tarjeta no asignada a ningún estudiante");
        }

        Libro libro = libroRepo.findById(idLibro)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (libro.getStock() == null || libro.getStock() <= 0) {
            throw new RuntimeException("No hay ejemplares disponibles");
        }

        libro.setStock(libro.getStock() - 1);
        libro.setDisponible(libro.getStock() > 0);
        libroRepo.save(libro);

        int dias = (diasPrestamo != null && diasPrestamo > 0) ? diasPrestamo : 7;
        LocalDateTime fechaDevolucion = LocalDateTime.now().plusDays(dias);

        RegistroBiblioteca registro = RegistroBiblioteca.builder()
                .estudiante(estudiante)
                .libro(libro)
                .fechaPrestamo(LocalDateTime.now())
                .fechaDevolucionEstimada(fechaDevolucion)
                .estado("PRESTADO")
                .build();

        return registroRepo.save(registro);
    }

    // ======================== DEVOLUCIÓN ========================
    @Transactional
    public RegistroBiblioteca registrarDevolucion(Long idPrestamo) {
        RegistroBiblioteca registro = registroRepo.findById(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if ("DEVUELTO".equalsIgnoreCase(registro.getEstado())) {
            throw new RuntimeException("El libro ya fue devuelto");
        }

        registro.setEstado("DEVUELTO");
        registro.setFechaDevolucionReal(LocalDateTime.now());
        registroRepo.save(registro);

        Libro libro = registro.getLibro();
        libro.setStock(libro.getStock() + 1);
        libro.setDisponible(true);
        libroRepo.save(libro);

        return registro;
    }

    // ======================== CONSULTAS ========================
    public List<Libro> listarLibrosDisponibles() {
        return libroRepo.findByDisponible(true);
    }

    public List<Libro> listarTodosLibros() {
        return libroRepo.findAll();
    }

    public List<RegistroBiblioteca> listarDevoluciones() {
        return registroRepo.findByEstadoOrderByFechaDevolucionRealDesc("DEVUELTO");
    }

    public List<RegistroBiblioteca> listarTodosPrestamos() {
        return registroRepo.findAll();
    }

    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepo.findByTituloContainingIgnoreCase(titulo);
    }

    public Libro guardarLibro(Libro libro) {
        return libroRepo.save(libro);
    }

    @Transactional
    public void eliminarLibro(Long id) {
        libroRepo.deleteById(id);
    }

    // ======================== EDITAR LIBRO ========================
    @Transactional
    public Libro actualizarLibro(Long id, LibroUpdateRequest req) {
        Libro libro = libroRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (req.getTitulo() != null) libro.setTitulo(req.getTitulo());
        if (req.getIsbn() != null) libro.setIsbn(req.getIsbn());
        if (req.getAutor() != null) libro.setAutor(req.getAutor());
        if (req.getEditorial() != null) libro.setEditorial(req.getEditorial());
        if (req.getAnio() != null) libro.setAnio(req.getAnio());
        if (req.getTipoMaterial() != null) libro.setTipoMaterial(req.getTipoMaterial());
        if (req.getCategoria() != null) libro.setCategoria(req.getCategoria());
        if (req.getStock() != null) {
            libro.setStock(req.getStock());
            libro.setDisponible(req.getStock() > 0);
        }

        return libroRepo.save(libro);
    }

    // ✅ Obtener préstamos activos de un estudiante
    public List<RegistroBiblioteca> obtenerPrestamosActivos(Long idEstudiante) {
        return registroRepo.findByEstudianteIdAndEstado(idEstudiante, "PRESTADO");
    }

    // ✅ Obtener historial completo de un estudiante
    public List<RegistroBiblioteca> obtenerHistorial(Long idEstudiante) {
        return registroRepo.findByEstudianteIdOrderByFechaPrestamoDesc(idEstudiante);
    }

    public List<PrestamoDTO> obtenerPrestamosPorEmail(String email) {
        Estudiante estudiante = estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        var registros = registroRepo.findByEstudiante(estudiante);

        return registros.stream()
                .map(r -> new PrestamoDTO(
                        r.getFechaPrestamo().toLocalDate(),
                        r.getLibro().getTitulo(),
                        r.getEstado()
                ))
                .toList();
    }
}
