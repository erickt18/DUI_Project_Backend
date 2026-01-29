package com.rfidcampus.rfid_campus.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rfidcampus.rfid_campus.model.RegistroBiblioteca;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.RegistroBibliotecaRepository;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

@Service
public class EstudianteService {

    private final TransaccionRepository transaccionRepo;
    private final RegistroBibliotecaRepository bibliotecaRepo;
    private final UsuarioRepository usuarioRepo;

    public EstudianteService(TransaccionRepository transaccionRepo, 
                             RegistroBibliotecaRepository bibliotecaRepo,
                             UsuarioRepository usuarioRepo) {
        this.transaccionRepo = transaccionRepo;
        this.bibliotecaRepo = bibliotecaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // 1. Obtener mis transacciones (Bar/Recargas)
    public List<Transaccion> obtenerMisTransacciones(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow();
        return transaccionRepo.findByUsuarioOrderByFechaDesc(usuario);
    }

    // 2. Obtener mis préstamos pendientes (Biblioteca)
    public List<RegistroBiblioteca> obtenerMisPrestamosPendientes(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow();
        return bibliotecaRepo.findByUsuarioAndEstado(usuario, "PRESTADO");
    }

    // ========================================================
    //  ALGORITMOS DE BÚSQUEDA 
    // ========================================================
    // A. BÚSQUEDA LINEAL: Buscar una transacción por palabra clave (ej: "Hamburguesa")
    public List<Transaccion> buscarTransaccionPorDetalle(String email, String palabraClave) {
        List<Transaccion> misMovimientos = obtenerMisTransacciones(email);
        
        // Algoritmo Lineal: Recorre uno por uno
        return misMovimientos.stream()
                .filter(t -> t.getDetalle().toLowerCase().contains(palabraClave.toLowerCase()))
                .collect(Collectors.toList());
    }
    // B. BÚSQUEDA BINARIA: Buscar una transacción por MONTO exacto
    public Transaccion buscarTransaccionPorMonto(String email, Double montoBuscado) {
        List<Transaccion> lista = obtenerMisTransacciones(email);
        
        // 1. Ordenar por monto (Requisito obligatorio para binaria)
        lista.sort(Comparator.comparing(Transaccion::getMonto));

        int inicio = 0;
        int fin = lista.size() - 1;

        while (inicio <= fin) {
            int medio = inicio + (fin - inicio) / 2;
            Double montoMedio = lista.get(medio).getMonto().doubleValue();

            if (montoMedio.equals(montoBuscado)) {
                return lista.get(medio); // ¡Encontrado!
            }
            if (montoMedio < montoBuscado) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return null; // No encontrado
    }
}