package com.rfidcampus.rfid_campus.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <--- NO OLVIDES ESTA IMPORTACIÓN
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rfidcampus.rfid_campus.model.Usuario;
import com.rfidcampus.rfid_campus.service.TarjetaService;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // 1. Contar cuántos son ESTUDIANTES
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = 'ROLE_ESTUDIANTE'")
    long countEstudiantes();

    // 2. Sumar el saldo de TODOS (Dinero en el sistema)
    // COALESCE es para que si no hay nadie, devuelva 0 en vez de error
    @Query("SELECT COALESCE(SUM(u.saldo), 0) FROM Usuario u")
    BigDecimal sumarSaldoTotal();

    // 3. Contar cuántos tienen tarjeta asignada
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.tarjeta IS NOT NULL")
    long countUsuariosConTarjeta();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true AND u.rol.nombre = 'ROLE_ESTUDIANTE'")
    long countEstudiantesActivos();

    @RestController
    @RequestMapping("/api/tarjetas")
    public class TarjetaController {

        private final TarjetaService tarjetaService;
        private final UsuarioRepository usuarioRepo; 
        private final TarjetaRfidRepository tarjetaRepo; 

        public TarjetaController(TarjetaService tarjetaService,
                UsuarioRepository usuarioRepo,
                TarjetaRfidRepository tarjetaRepo) {
            this.tarjetaService = tarjetaService;
            this.usuarioRepo = usuarioRepo;
            this.tarjetaRepo = tarjetaRepo;
        }

       
    }

}
