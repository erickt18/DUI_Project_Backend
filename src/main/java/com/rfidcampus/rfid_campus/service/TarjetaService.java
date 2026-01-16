package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.dto.CompraRequest;
import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.model.Producto;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.UsuarioRepository;
import com.rfidcampus.rfid_campus.repository.ProductoRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TarjetaService {

    private final TarjetaRfidRepository tarjetaRepo;
    private final UsuarioRepository usuarioRepo; // Antes estudianteRepo
    private final TransaccionRepository transaccionRepo;
    private final ProductoRepository productoRepo;

    public TarjetaService(TarjetaRfidRepository tarjetaRepo, UsuarioRepository usuarioRepo,
                          TransaccionRepository transaccionRepo, ProductoRepository productoRepo) {
        this.tarjetaRepo = tarjetaRepo;
        this.usuarioRepo = usuarioRepo;
        this.transaccionRepo = transaccionRepo;
        this.productoRepo = productoRepo;
    }

    // ✅ MÉTODO CORREGIDO: Usa CompraRequest y BigDecimal
    @Transactional
    public Map<String, Object> procesarCompraMultiple(CompraRequest req) {
        
        // 1. Validar tarjeta
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUid(req.getTarjetaUid())
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (!"ACTIVA".equalsIgnoreCase(tarjeta.getEstado())) {
            throw new RuntimeException("La tarjeta está BLOQUEADA o inactiva");
        }

        // 2. Obtener Usuario
        Usuario usuario = tarjeta.getUsuario();
        if (usuario == null) {
            throw new RuntimeException("Tarjeta sin usuario asignado");
        }

        // 3. Validar productos
        List<Long> ids = req.getProductosIds();
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        List<Producto> productos = productoRepo.findAllById(ids);
        if (productos.size() != ids.size()) {
            throw new RuntimeException("Uno o más productos no existen en la base de datos");
        }

        // 4. Calcular Total (Usando BigDecimal)
        BigDecimal total = productos.stream()
                .map(Producto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Validar Saldo
        if (usuario.getSaldo().compareTo(total) < 0) {
            throw new RuntimeException("Saldo insuficiente. Saldo actual: $" + usuario.getSaldo());
        }

        // 6. Cobrar (Resta exacta)
        usuario.setSaldo(usuario.getSaldo().subtract(total));
        usuarioRepo.save(usuario);

        // 7. Registrar Transacciones (Una por producto para detalle)
        // Opcional: Podrías hacer una sola transacción global si prefieres
        List<String> nombresProductos = new ArrayList<>();
        
        for (Producto p : productos) {
            Transaccion t = new Transaccion();
            t.setUsuario(usuario); // Asegúrate que Transaccion tenga setUsuario
            t.setTipo("COMPRA_BAR");
            t.setMonto(p.getPrecio().negate()); // Negativo porque es gasto
            t.setDetalle(p.getNombre());
            t.setFecha(LocalDateTime.now());
            transaccionRepo.save(t);
            
            nombresProductos.add(p.getNombre());
        }

        // 8. Retornar resumen
        return Map.of(
            "status", "success",
            "nuevoSaldo", usuario.getSaldo(),
            "totalCobrado", total,
            "productos", nombresProductos
        );
    }
}