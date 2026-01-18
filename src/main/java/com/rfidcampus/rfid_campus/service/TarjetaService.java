package com.rfidcampus.rfid_campus.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // ✅ Usamos Usuario

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rfidcampus.rfid_campus.dto.CompraRequest;
import com.rfidcampus.rfid_campus.model.Producto; // ✅ Usamos UsuarioRepository
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.repository.ProductoRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;

@Service
public class TarjetaService {

    private final TarjetaRfidRepository tarjetaRepo;
    private final UsuarioRepository usuarioRepo;
    private final TransaccionRepository transaccionRepo;
    private final ProductoRepository productoRepo;

    public TarjetaService(TarjetaRfidRepository tarjetaRepo, UsuarioRepository usuarioRepo,
                          TransaccionRepository transaccionRepo, ProductoRepository productoRepo) {
        this.tarjetaRepo = tarjetaRepo;
        this.usuarioRepo = usuarioRepo;
        this.transaccionRepo = transaccionRepo;
        this.productoRepo = productoRepo;
    }

    // ==========================================
    // 1. GESTIÓN BÁSICA (Listar, Asignar, Bloquear)
    // ==========================================

    public List<TarjetaRfid> listar() {
        return tarjetaRepo.findAll();
    }

    @Transactional
    public void asignarTarjeta(String uid, Long usuarioId) {
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUid(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada en el sistema"));

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar si la tarjeta ya es de otro
        if (tarjeta.getUsuario() != null && !tarjeta.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Esta tarjeta ya pertenece a otro usuario");
        }

        // Asignación bidireccional
        tarjeta.setUsuario(usuario);
        usuario.setUidTarjeta(uid);

        usuarioRepo.save(usuario);
        tarjetaRepo.save(tarjeta);
    }

    @Transactional
    public void bloquearTarjeta(String uid) {
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUid(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        tarjeta.setEstado("BLOQUEADA");
        tarjetaRepo.save(tarjeta);
    }

    @Transactional
    public void desbloquearTarjeta(String uid) {
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUid(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        tarjeta.setEstado("ACTIVA");
        tarjetaRepo.save(tarjeta);
    }

    // ==========================================
    // 2. LÓGICA FINANCIERA (Recargas y Compras)
    // ==========================================

    @Transactional
    public Usuario recargarSaldo(String uid, Double montoDouble) {
        // Buscamos tarjeta activa
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUid(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if ("BLOQUEADA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("La tarjeta está bloqueada");
        }

        Usuario usuario = tarjeta.getUsuario();
        if (usuario == null) throw new RuntimeException("Tarjeta sin usuario asignado");

        // Conversión segura a BigDecimal
        BigDecimal monto = BigDecimal.valueOf(montoDouble);
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto debe ser positivo");
        }

        // Sumar saldo
        usuario.setSaldo(usuario.getSaldo().add(monto));
        usuarioRepo.save(usuario);

        // Registrar transacción
        transaccionRepo.save(Transaccion.builder()
                .usuario(usuario)
                .tipo("RECARGA")
                .monto(monto) // Positivo
                .detalle("Recarga de saldo")
                .fecha(LocalDateTime.now())
                .build());

        return usuario;
    }

    // ✅ MÉTODO DE COMPRA (El que arreglamos antes)
    @Transactional
    public Map<String, Object> procesarCompraMultiple(CompraRequest req) {
        
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUid(req.getTarjetaUid())
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (!"ACTIVA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("La tarjeta está BLOQUEADA o inactiva");
        }

        Usuario usuario = tarjeta.getUsuario();
        if (usuario == null) throw new RuntimeException("Tarjeta sin usuario asignado");

        List<Long> ids = req.getProductosIds();
        if (ids == null || ids.isEmpty()) throw new RuntimeException("El carrito está vacío");

        List<Producto> productos = productoRepo.findAllById(ids);
        if (productos.size() != ids.size()) throw new RuntimeException("Productos no válidos");

        // Calcular Total
        BigDecimal total = productos.stream()
                .map(Producto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validar Saldo
        if (usuario.getSaldo().compareTo(total) < 0) {
            throw new RuntimeException("Saldo insuficiente. Tienes: $" + usuario.getSaldo());
        }

        // Cobrar
        usuario.setSaldo(usuario.getSaldo().subtract(total));
        usuarioRepo.save(usuario);

        // Registrar Transacciones
        List<String> nombresProductos = new ArrayList<>();
        for (Producto p : productos) {
            Transaccion t = new Transaccion();
            t.setUsuario(usuario);
            t.setTipo("COMPRA_BAR");
            t.setMonto(p.getPrecio().negate()); // Negativo para indicar gasto en reportes
            t.setDetalle(p.getNombre());
            t.setFecha(LocalDateTime.now());
            transaccionRepo.save(t);
            nombresProductos.add(p.getNombre());
        }

        return Map.of(
            "status", "success",
            "nuevoSaldo", usuario.getSaldo(),
            "totalCobrado", total,
            "productos", nombresProductos
        );
    }
}