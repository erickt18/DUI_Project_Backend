package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.dto.CompraMultipleRequest;
import com.rfidcampus.rfid_campus.dto.CompraRequest;
import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.model.Producto;
import com.rfidcampus.rfid_campus.model.TarjetaRfid;
import com.rfidcampus.rfid_campus.model.Transaccion;
import com.rfidcampus.rfid_campus.repository.EstudianteRepository;
import com.rfidcampus.rfid_campus.repository.ProductoRepository;
import com.rfidcampus.rfid_campus.repository.TarjetaRfidRepository;
import com.rfidcampus.rfid_campus.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TarjetaService {

    private final TarjetaRfidRepository tarjetaRepo;
    private final EstudianteRepository estudianteRepo;
    private final TransaccionRepository transaccionRepo;
    private final ProductoRepository productoRepo;

    public TarjetaService(
            TarjetaRfidRepository tarjetaRepo,
            EstudianteRepository estudianteRepo,
            TransaccionRepository transaccionRepo,
            ProductoRepository productoRepo) {
        this.tarjetaRepo = tarjetaRepo;
        this.estudianteRepo = estudianteRepo;
        this.transaccionRepo = transaccionRepo;
        this.productoRepo = productoRepo;
    }

    public TarjetaRfid guardar(TarjetaRfid tarjeta) {
        if (tarjeta.getTarjetaUid() != null) {
            tarjeta.setTarjetaUid(normaliza(tarjeta.getTarjetaUid()));
        }
        return tarjetaRepo.save(tarjeta);
    }

    public List<TarjetaRfid> listar() {
        return tarjetaRepo.findAll();
    }

    // ✅ Búsqueda robusta por UID (trim)
    public TarjetaRfid buscarPorUid(String uid) {
        String u = normaliza(uid);
        return tarjetaRepo.findByTarjetaUid(u)
                .orElseThrow(() -> new RuntimeException("Tarjeta no existe en el sistema"));
    }

    @Transactional
    public TarjetaRfid asignarTarjeta(String tarjetaUid, Long idEstudiante) {
        TarjetaRfid tarjeta = buscarPorUid(tarjetaUid);
        Estudiante estudiante = estudianteRepo.findById(idEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        if (tarjeta.getEstudiante() != null
                && !tarjeta.getEstudiante().getId().equals(idEstudiante)) {
            throw new RuntimeException("La tarjeta ya está asignada a otro estudiante");
        }

        tarjeta.setEstudiante(estudiante);
        estudiante.setUidTarjeta(tarjeta.getTarjetaUid());
        estudianteRepo.save(estudiante);
        return tarjetaRepo.save(tarjeta);
    }

    @Transactional
    public Estudiante recargarSaldo(String uid, Double monto) {
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUidAndEstado(normaliza(uid), "ACTIVA")
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada o inactiva"));

        Estudiante est = tarjeta.getEstudiante();
        est.setSaldo(est.getSaldo() + monto);
        estudianteRepo.save(est);

        transaccionRepo.save(
                Transaccion.builder()
                        .estudiante(est)
                        .tipo("RECARGA")
                        .monto(monto)
                        .fecha(LocalDateTime.now())
                        .build());

        return est;
    }

    public Double consultarSaldo(String uid) {
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUidAndEstado(normaliza(uid), "ACTIVA")
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada o inactiva"));
        return tarjeta.getEstudiante().getSaldo();
    }

    @Transactional
    public Estudiante pagar(String uid, Double monto) {
        TarjetaRfid tarjeta = tarjetaRepo.findByTarjetaUidAndEstado(normaliza(uid), "ACTIVA")
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada o inactiva"));

        Estudiante est = tarjeta.getEstudiante();
        if (est.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente");
        }

        est.setSaldo(est.getSaldo() - monto);
        estudianteRepo.save(est);

        transaccionRepo.save(
                Transaccion.builder()
                        .estudiante(est)
                        .tipo("COMPRA_BAR")
                        .monto(monto)
                        .fecha(LocalDateTime.now())
                        .build());

        return est;
    }

    @Transactional
    public TarjetaRfid bloquearTarjeta(String uid) {
        TarjetaRfid t = buscarPorUid(uid);
        t.setEstado("BLOQUEADA");
        return tarjetaRepo.save(t);
    }

    @Transactional
    public TarjetaRfid desbloquearTarjeta(String uid) {
        TarjetaRfid t = buscarPorUid(uid);
        t.setEstado("ACTIVA");
        return tarjetaRepo.save(t);
    }

    @Transactional
    public TarjetaRfid bloquearTarjetaPorEstudiante(String email) {
        Estudiante est = estudianteRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        TarjetaRfid tarjeta = tarjetaRepo.findByEstudiante(est)
                .orElseThrow(() -> new RuntimeException("No tienes una tarjeta asignada"));

        tarjeta.setEstado("BLOQUEADA");
        return tarjetaRepo.save(tarjeta);
    }

    private String normaliza(String s) {
        return s == null ? null : s.trim();
    }

    // ✅ COBRO DE PRODUCTO POR TARJETA (guarda detalle = nombre del producto)
    @Transactional
    public Map<String, Object> procesarCompraMultiple(CompraMultipleRequest req) {
        // 1. Buscar la tarjeta
        TarjetaRfid tarjeta = tarjetaRepo
                .findByTarjetaUidAndEstado(normaliza(req.getTarjetaUid()), "ACTIVA")
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada o inactiva"));

        Estudiante est = tarjeta.getEstudiante();
        if (est == null) {
            throw new RuntimeException("La tarjeta no está asignada a ningún estudiante");
        }

        // 2. Obtener todos los productos y el total
        List<Long> ids = req.getProductoIds();
        if (ids == null || ids.isEmpty())
            throw new RuntimeException("No se seleccionaron productos");

        List<Producto> productos = productoRepo.findAllById(ids);
        if (productos.size() != ids.size())
            throw new RuntimeException("Uno o más productos no existen");

        double total = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue())
                .sum();

        // 3. Validar saldo suficiente
        if (est.getSaldo() < total)
            throw new RuntimeException("Saldo insuficiente");

        // 4. Descontar saldo
        double saldoNuevo = est.getSaldo() - total;
        est.setSaldo(saldoNuevo);
        estudianteRepo.save(est);

        // 5. Registrar una transacción por cada producto
        List<Map<String, Object>> compras = new ArrayList<>();
        for (Producto p : productos) {
            double precio = p.getPrecio().doubleValue();
            // Puedes agrupar todas en una sola transacción si prefieres (ver nota abajo)
            transaccionRepo.save(
                    Transaccion.builder()
                            .estudiante(est)
                            .tipo("COMPRA_PRODUCTO")
                            .monto(precio)
                            .detalle(p.getNombre())
                            .fecha(LocalDateTime.now())
                            .build());
            compras.add(Map.of(
                    "producto", p.getNombre(),
                    "precio", precio));
        }

        // 6. Respuesta
        return Map.of(
                "success", true,
                "nuevoSaldo", saldoNuevo,
                "compras", compras,
                "total", total);
    }
}
