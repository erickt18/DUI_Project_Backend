package com.rfidcampus.rfid_campus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.springframework.stereotype.Service;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepo;

    public TransaccionService(TransaccionRepository transaccionRepo) {
        this.transaccionRepo = transaccionRepo;
    }

    // ✅ MÉTODO QUE FALTABA (Necesario para el endpoint normal del controlador)
    public List<Transaccion> obtenerHistorialPorUsuario(Long idUsuario) {
        return transaccionRepo.findByUsuarioIdOrderByFechaDesc(idUsuario);
    }

    // ✅ IMPLEMENTACIÓN DE PILA (STACK) - LIFO
    // Obtiene las transacciones y las devuelve en orden inverso usando una Pila manual
    public List<Transaccion> obtenerHistorialPila(Long idUsuario) {
        // 1. Obtenemos datos crudos de la BD
        List<Transaccion> listaOriginal = transaccionRepo.findByUsuarioIdOrderByFechaDesc(idUsuario);
        
        // 2. Creamos la PILA
        Stack<Transaccion> pilaTransacciones = new Stack<>();
        
        // 3. Apilamos (Push) - Llenamos la pila
        for (Transaccion t : listaOriginal) {
            pilaTransacciones.push(t);
        }

        // 4. Desapilamos (Pop) - Vaciamos la pila en una nueva lista
        // Esto demuestra el uso de LIFO (Last In, First Out)
        List<Transaccion> historialInverso = new ArrayList<>();
        while (!pilaTransacciones.isEmpty()) {
            historialInverso.add(pilaTransacciones.pop());
        }
        
        return historialInverso; 
    }

    // Métodos estándar
    public List<Transaccion> obtenerUltimasTransacciones() {
        return transaccionRepo.findTop100ByOrderByFechaDesc();
    }
    
    public List<Transaccion> buscarPorEmailYTipo(String email, String tipo) {
        return transaccionRepo.findByUsuarioEmailAndTipoOrderByFechaDesc(email, tipo);
    }
    
    public Transaccion guardar(Transaccion t) {
        return transaccionRepo.save(t);
    }
}