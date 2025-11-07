package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.Libro;
import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
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

    public BibliotecaService(LibroRepository libroRepo,
                            RegistroBibliotecaRepository registroRepo,
                            TarjetaRfidRepository tarjetaRepo) {
        this.libroRepo = libroRepo;
        this.registroRepo = registroRepo;
        this.tarjetaRepo = tarjetaRepo;
    }

    // Registrar préstamo
    @Transactional
    public RegistroBiblioteca registrarPrestamo(String uid, Long idLibro, Integer diasPrestamo) {
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUidAndEstado(uid, "ACTIVA")
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada o inactiva"));

        Estudiante estudiante = tarjeta.getEstudiante();
        if (estudiante == null) {
            throw new RuntimeException("Tarjeta no asignada a ningún estudiante");
        }

        Libro libro = libroRepo.findById(idLibro)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (!libro.getDisponible()) {
            throw new RuntimeException("Libro no disponible");
        }

        libro.setDisponible(false);
        libroRepo.save(libro);

        int dias = diasPrestamo != null ? diasPrestamo : 7;
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

    // Registrar devolución
    @Transactional
    public RegistroBiblioteca registrarDevolucion(Long idPrestamo) {
        RegistroBiblioteca registro = registroRepo.findById(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if ("DEVUELTO".equals(registro.getEstado())) {
            throw new RuntimeException("El libro ya fue devuelto");
        }

        registro.setEstado("DEVUELTO");
        registro.setFechaDevolucionReal(LocalDateTime.now());
        registroRepo.save(registro);

        Libro libro = registro.getLibro();
        libro.setDisponible(true);
        libroRepo.save(libro);

        return registro;
    }

    // Obtener préstamos activos de un estudiante
    public List<RegistroBiblioteca> obtenerPrestamosActivos(Long idEstudiante) {
        return registroRepo.findByEstudianteIdAndEstado(idEstudiante, "PRESTADO");
    }

    // Obtener historial completo de un estudiante
    public List<RegistroBiblioteca> obtenerHistorial(Long idEstudiante) {
        return registroRepo.findByEstudianteIdOrderByFechaPrestamoDesc(idEstudiante);
    }

    // Listar todos los libros disponibles
    public List<Libro> listarLibrosDisponibles() {
        return libroRepo.findByDisponible(true);
    }

    // Buscar libros por título
    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepo.findByTituloContainingIgnoreCase(titulo);
    }

    // Guardar libro (para catálogo)
    public Libro guardarLibro(Libro libro) {
        return libroRepo.save(libro);
    }

    // Eliminar libro por id (NUEVO)
    @Transactional
    public void eliminarLibro(Long id) {
        libroRepo.deleteById(id);
    }
}
