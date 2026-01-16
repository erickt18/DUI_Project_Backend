package com.rfidcampus.rfid_campus.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UsuarioRepository usuarioRepository; // ✅ Cambio de nombre

    // ✅ ESTRUCTURA DE DATOS: COLA (Queue) para Lista de Espera
    private Map<Long, Queue<String>> colaEsperaLibros = new HashMap<>();

    public BibliotecaService(
            LibroRepository libroRepo,
            RegistroBibliotecaRepository registroRepo,
            TarjetaRfidRepository tarjetaRepo,
            UsuarioRepository usuarioRepository) { // ✅ Inyectamos el nuevo repo
        this.libroRepo = libroRepo;
        this.registroRepo = registroRepo;
        this.tarjetaRepo = tarjetaRepo;
        this.usuarioRepository = usuarioRepository;
    }

    // ======================== PRÉSTAMO ========================
    @Transactional
    public RegistroBiblioteca registrarPrestamo(String uid, Long idLibro, Integer diasPrestamo) {

        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUid(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if ("BLOQUEADA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("Tarjeta bloqueada.");
        }

        // ✅ Usamos Usuario
        Usuario usuario = tarjeta.getUsuario(); 
        if (usuario == null) {
            throw new RuntimeException("Tarjeta no asignada a ningún usuario");
        }

        Libro libro = libroRepo.findById(idLibro)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (libro.getStock() <= 0) {
            // Lógica de Cola: Si no hay stock, sugerir entrar a lista de espera
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

        // ✅ Verificar Cola de Espera
        notificarSiguienteEnCola(libro.getId());

        return registro;
    }

    // Métodos de COLA
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
            // Aquí podrías enviar un email real
        }
    }

    // ... (El resto de métodos de búsqueda se mantienen igual, solo cambia los Repositorios) ...
    
    public List<PrestamoDTO> obtenerPrestamosPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var registros = registroRepo.findByUsuario(usuario); // ✅ findByUsuario

        return registros.stream()
                .map(r -> new PrestamoDTO(
                        r.getFechaPrestamo().toLocalDate(),
                        r.getLibro().getTitulo(),
                        r.getEstado()
                ))
                .toList();
    }
}