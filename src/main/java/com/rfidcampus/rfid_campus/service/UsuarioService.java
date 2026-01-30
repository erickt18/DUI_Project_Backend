package com.rfidcampus.rfid_campus.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rfidcampus.rfid_campus.model.Rol;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.RolRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TarjetaRfidRepository tarjetaRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, 
                         TarjetaRfidRepository tarjetaRepository,
                         RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.rolRepository = rolRepository;
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

    // ✅ MÉTODO CORREGIDO: ASIGNAR ROL Y TARJETA (CON ACTUALIZACIÓN)
    @Transactional
    public Usuario asignarDatos(Long id, String rol, String tarjetaUid) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. ASIGNAR ROL
        if (rol != null && !rol.isEmpty()) {
            Rol nuevoRol = rolRepository.findByNombre(rol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rol));
            usuario.setRol(nuevoRol);
            System.out.println("✅ Rol asignado: " + nuevoRol.getNombre());
        }

        // 2. ASIGNAR O ACTUALIZAR TARJETA RFID
        if (tarjetaUid != null && !tarjetaUid.trim().isEmpty()) {
            String uidLimpio = tarjetaUid.trim();
            
            // ✅ PASO A: Verificar si el usuario ya tiene una tarjeta asignada
            Optional<TarjetaRfid> tarjetaActualOpt = tarjetaRepository.findByUsuarioId(id);
            
            if (tarjetaActualOpt.isPresent()) {
                TarjetaRfid tarjetaActual = tarjetaActualOpt.get();
                
                // ✅ El usuario YA tiene tarjeta -> VERIFICAR si necesita actualización
                if (!tarjetaActual.getTarjetaUid().equals(uidLimpio)) {
                    System.out.println("🔄 Actualizando tarjeta del usuario " + id);
                    System.out.println("   Tarjeta antigua: " + tarjetaActual.getTarjetaUid());
                    System.out.println("   Tarjeta nueva: " + uidLimpio);
                    
                    // Verificar si el nuevo UID ya existe y pertenece a otro usuario
                    Optional<TarjetaRfid> tarjetaConNuevoUidOpt = tarjetaRepository.findById(uidLimpio);
                    if (tarjetaConNuevoUidOpt.isPresent()) {
                        TarjetaRfid tarjetaConNuevoUid = tarjetaConNuevoUidOpt.get();
                        if (tarjetaConNuevoUid.getUsuario() != null && 
                            !tarjetaConNuevoUid.getUsuario().getId().equals(id)) {
                            throw new RuntimeException("El UID " + uidLimpio + " ya está asignado a otro usuario");
                        }
                    }
                    
                    // Eliminar la tarjeta antigua
                    tarjetaRepository.delete(tarjetaActual);
                    tarjetaRepository.flush(); // Forzar la eliminación inmediata
                    
                    // Crear la nueva tarjeta con el nuevo UID
                    TarjetaRfid nuevaTarjeta = new TarjetaRfid();
                    nuevaTarjeta.setTarjetaUid(uidLimpio);
                    nuevaTarjeta.setEstado("ACTIVA");
                    nuevaTarjeta.setUsuario(usuario);
                    tarjetaRepository.save(nuevaTarjeta);
                    System.out.println("✅ Tarjeta reemplazada: " + uidLimpio);
                } else {
                    System.out.println("ℹ️  El usuario ya tiene esta tarjeta asignada: " + uidLimpio);
                }
            } else {
                // ✅ PASO B: El usuario NO tiene tarjeta -> CREAR una nueva
                Optional<TarjetaRfid> tarjetaConUidOpt = tarjetaRepository.findById(uidLimpio);
                
                if (tarjetaConUidOpt.isPresent()) {
                    TarjetaRfid tarjetaConUid = tarjetaConUidOpt.get();
                    
                    // El UID ya existe, verificar si está asignado a otro usuario
                    if (tarjetaConUid.getUsuario() != null) {
                        throw new RuntimeException("Este UID ya está asignado a otro usuario");
                    }
                    
                    tarjetaConUid.setUsuario(usuario);
                    tarjetaConUid.setEstado("ACTIVA");
                    tarjetaRepository.save(tarjetaConUid);
                    System.out.println("✅ Tarjeta existente asignada: " + uidLimpio);
                } else {
                    // Crear nueva tarjeta
                    TarjetaRfid nuevaTarjeta = new TarjetaRfid();
                    nuevaTarjeta.setTarjetaUid(uidLimpio);
                    nuevaTarjeta.setEstado("ACTIVA");
                    nuevaTarjeta.setUsuario(usuario);
                    tarjetaRepository.save(nuevaTarjeta);
                    System.out.println("✅ Nueva tarjeta RFID creada: " + uidLimpio);
                }
            }
        }

        // 3. GUARDAR USUARIO
        return usuarioRepository.save(usuario);
    }

    // ✅ MÉTODO PARA ESTADÍSTICAS DEL DASHBOARD
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        long totalUsuarios = usuarios.size();
        long totalEstudiantes = usuarios.stream()
                .filter(u -> "ROLE_ESTUDIANTE".equals(u.getRolNombre()))
                .count();
        
        BigDecimal dineroTotal = usuarios.stream()
                .map(Usuario::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long tarjetasAsignadas = usuarios.stream()
                .filter(u -> u.getUidTarjeta() != null)
                .count();
        
        int porcentajeTarjetas = totalUsuarios > 0 
                ? (int) ((tarjetasAsignadas * 100) / totalUsuarios) 
                : 0;
        
        return Map.of(
                "totalUsuarios", totalUsuarios,
                "totalEstudiantes", totalEstudiantes,
                "dineroTotal", dineroTotal,
                "tarjetasAsignadas", porcentajeTarjetas
        );
    }
}
