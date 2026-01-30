package com.rfidcampus.rfid_campus.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rfidcampus.rfid_campus.dto.CompraRequest;
import com.rfidcampus.rfid_campus.model.Producto;
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

    public List<TarjetaRfid> listar() {
        return tarjetaRepo.findAll();
    }

    @Transactional
    public void asignarTarjeta(String uid, Long usuarioId) {
        TarjetaRfid tarjeta = tarjetaRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada en el sistema"));

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (tarjeta.getUsuario() != null && !tarjeta.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Esta tarjeta ya pertenece a otro usuario");
        }

        tarjeta.setUsuario(usuario);
        usuarioRepo.save(usuario);
        tarjetaRepo.save(tarjeta);
    }

    @Transactional
    public void bloquearTarjeta(String uid) {
        TarjetaRfid tarjeta = tarjetaRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        tarjeta.setEstado("BLOQUEADA");
        tarjetaRepo.save(tarjeta);
    }

    @Transactional
    public void desbloquearTarjeta(String uid) {
        TarjetaRfid tarjeta = tarjetaRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        tarjeta.setEstado("ACTIVA");
        tarjetaRepo.save(tarjeta);
    }

    @Transactional
    public Usuario recargarSaldo(String uid, Double montoDouble) {
        TarjetaRfid tarjeta = tarjetaRepo.findById(uid)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if ("BLOQUEADA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("La tarjeta está bloqueada");
        }

        Usuario usuario = tarjeta.getUsuario();
        if (usuario == null) {
            throw new RuntimeException("Tarjeta sin usuario asignado");
        }

        BigDecimal monto = BigDecimal.valueOf(montoDouble);
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto debe ser positivo");
        }

        usuario.setSaldo(usuario.getSaldo().add(monto));
        usuarioRepo.save(usuario);

        transaccionRepo.save(Transaccion.builder()
                .usuario(usuario)
                .tipo("RECARGA")
                .monto(monto)
                .detalle("Recarga de saldo")
                .fecha(LocalDateTime.now())
                .build());

        return usuario;
    }

    @Transactional
    public Map<String, Object> procesarCompraMultiple(CompraRequest req) {
        TarjetaRfid tarjeta = tarjetaRepo.findById(req.getTarjetaUid())
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (!"ACTIVA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("La tarjeta está BLOQUEADA o inactiva");
        }

        Usuario usuario = tarjeta.getUsuario();
        if (usuario == null) {
            throw new RuntimeException("Tarjeta sin usuario asignado");
        }

        List<Long> ids = req.getProductosIds();
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        List<Producto> productos = productoRepo.findAllById(ids);

        BigDecimal total = productos.stream()
                .map(Producto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (usuario.getSaldo().compareTo(total) < 0) {
            throw new RuntimeException("Saldo insuficiente. Tienes: $" + usuario.getSaldo());
        }

        usuario.setSaldo(usuario.getSaldo().subtract(total));
        usuarioRepo.save(usuario);

        List<String> nombresProductos = new ArrayList<>();
        for (Producto p : productos) {
            Transaccion t = new Transaccion();
            t.setUsuario(usuario);
            t.setTipo("COMPRA_BAR");
            t.setMonto(p.getPrecio().negate());
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

    @Transactional
    public void bloquearTarjetaPorEmail(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        TarjetaRfid tarjeta = tarjetaRepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("No tienes una tarjeta asignada para bloquear."));

        tarjeta.setEstado("BLOQUEADA");
        tarjetaRepo.save(tarjeta);
    }

    // ✅ MÉTODO CORREGIDO - USAR findById
    public TarjetaRfid buscarPorUid(String uid) {
        return tarjetaRepo.findById(uid).orElse(null);
    }
}
