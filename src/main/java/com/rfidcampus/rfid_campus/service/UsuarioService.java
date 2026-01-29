package com.rfidcampus.rfid_campus.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TarjetaRfidRepository tarjetaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, TarjetaRfidRepository tarjetaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    
    public Optional<Usuario> buscarPorUid(String uid) {
        return tarjetaRepository.findById(uid)
                .map(tarjeta -> tarjeta.getUsuario()); 
    }
    
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}