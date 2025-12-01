package com.rfidcampus.rfid_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository repo;

    public List<Transaccion> ultimas100() {
        return repo.findTop100ByOrderByFechaDesc();
    }

    public List<Transaccion> historialEstudiante(Long id) {
        return repo.findByEstudianteIdOrderByFechaDesc(id);
    }

    public List<Transaccion> todas() {
        return repo.findAll();
    }
}

