package com.rfidcampus.rfid_campus.repository;

import com.rfidcampus.rfid_campus.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
        SELECT p FROM Producto p
        WHERE (:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:activo IS NULL OR p.activo = :activo)
        """)
    Page<Producto> search(@Param("q") String q,
                          @Param("activo") Boolean activo,
                          Pageable pageable);
}
