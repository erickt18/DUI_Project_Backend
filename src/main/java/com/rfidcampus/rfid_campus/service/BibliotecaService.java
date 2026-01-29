package com.rfidcampus.rfid_campus.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rfidcampus.rfid_campus.dto.LibroUpdateRequest;
import com.rfidcampus.rfid_campus.dto.PrestamoDTO;
import com.rfidcampus.rfid_campus.model.Libro;
import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.LibroRepository;
import com.rfidcampus.rfid_campus.repository.RegistroBibliotecaRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

@Service
public class BibliotecaService {

    private final LibroRepository libroRepo;
    private final RegistroBibliotecaRepository registroRepo;
    private final TarjetaRfidRepository tarjetaRepo;
    private final UsuarioRepository usuarioRepository;

    private Map<Long, Queue<String>> colaEsperaLibros = new HashMap<>();

    public BibliotecaService(LibroRepository libroRepo, RegistroBibliotecaRepository registroRepo,
                             TarjetaRfidRepository tarjetaRepo, UsuarioRepository usuarioRepository) {
        this.libroRepo = libroRepo;
        this.registroRepo = registroRepo;
        this.tarjetaRepo = tarjetaRepo;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RegistroBiblioteca registrarPrestamo(String uid, Long idLibro, Integer diasPrestamo) {
        TarjetaRfid tarjeta = tarjetaRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if ("BLOQUEADA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("Tarjeta bloqueada.");
        }

        Usuario usuario = tarjeta.getUsuario();
        if (usuario == null) {
            throw new RuntimeException("Tarjeta no asignada a ningún usuario");
        }

        Libro libro = libroRepo.findById(idLibro)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (libro.getStock() <= 0) {
            throw new RuntimeException("No hay ejemplares. Use la función 'entrar en lista de espera'.");
        }

        libro.setStock(libro.getStock() - 1);
        libro.setDisponible(libro.getStock() > 0);
        libroRepo.save(libro);

        int dias = (diasPrestamo != null && diasPrestamo > 0) ? diasPrestamo : 7;

        RegistroBiblioteca registro = RegistroBiblioteca.builder()
                .usuario(usuario)
                .libro(libro)
                .fechaPrestamo(LocalDateTime.now())
                .fechaDevolucionEstimada(LocalDateTime.now().plusDays(dias))
                .estado("PRESTADO")
                .build();

        return registroRepo.save(registro);
    }

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

        notificarSiguienteEnCola(libro.getId());

        return registro;
    }

    public String agregarAColaEspera(Long idLibro, String emailUsuario) {
        colaEsperaLibros.putIfAbsent(idLibro, new LinkedList<>());
        colaEsperaLibros.get(idLibro).offer(emailUsuario);
        return "Agregado a la cola. Posición: " + colaEsperaLibros.get(idLibro).size();
    }

    private void notificarSiguienteEnCola(Long idLibro) {
        Queue<String> cola = colaEsperaLibros.get(idLibro);
        if (cola != null && !cola.isEmpty()) {
            String email = cola.poll();
            System.out.println(">>> NOTIFICACIÓN: El libro ID " + idLibro + " está disponible para " + email);
        }
    }

    public List<PrestamoDTO> obtenerPrestamosPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        var registros = registroRepo.findByUsuario(usuario);

        return registros.stream()
                .map(r -> new PrestamoDTO(
                r.getFechaPrestamo().toLocalDate(),
                r.getLibro().getTitulo(),
                r.getEstado()
        ))
                .toList();
    }

    public List<Libro> listarTodosLibros() {
        return libroRepo.findAll();
    }

    public List<Libro> listarLibrosDisponibles() {
        return libroRepo.findByDisponible(true);// AQUI ME DA ERROR 
    }

    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepo.findByTituloContainingIgnoreCase(titulo);
    }

    
    public List<Libro> buscarPorTipo(String tipo) {
        return libroRepo.findByTipoMaterialIgnoreCase(tipo);
    }

    public List<RegistroBiblioteca> listarTodosPrestamos() {
        return registroRepo.findAll();
    }

    public Libro guardarLibro(Libro libro) {
        return libroRepo.save(libro);
    }

    public void eliminarLibro(Long id) {
        libroRepo.deleteById(id);
    }

    public Libro actualizarLibro(Long id, LibroUpdateRequest req) {
        Libro libro = libroRepo.findById(id).orElseThrow();
        if (req.getTitulo() != null) {
            libro.setTitulo(req.getTitulo());
        }
        if (req.getStock() != null) {
            libro.setStock(req.getStock());
            libro.setDisponible(req.getStock() > 0);
        }
        return libroRepo.save(libro);
    }
}