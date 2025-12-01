package com.rfidcampus.rfid_campus.service;

import com.rfidcampus.rfid_campus.dto.ProductoRequest;
import com.rfidcampus.rfid_campus.dto.ProductoResponse;
import com.rfidcampus.rfid_campus.model.Producto;
import com.rfidcampus.rfid_campus.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repo;

    private static ProductoResponse toDTO(Producto p){
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getActivo()
        );
    }

    private static void apply(ProductoRequest in, Producto p){
        p.setNombre(in.nombre());
        p.setDescripcion(in.descripcion());
        p.setPrecio(in.precio());
        p.setStock(in.stock());
        p.setActivo(in.activo());
    }

    @Transactional
    public ProductoResponse create(ProductoRequest in){
        Producto p = new Producto();
        apply(in, p);
        return toDTO(repo.save(p));
    }

    @Transactional
    public ProductoResponse update(Long id, ProductoRequest in){
        Producto p = repo.findById(id).orElseThrow();
        apply(in, p);
        return toDTO(p);
    }

    @Transactional
    public void delete(Long id){
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProductoResponse get(Long id){
        return repo.findById(id).map(ProductoService::toDTO).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> list(String q, Boolean activo, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return repo.search((q==null||q.isBlank())? null : q.trim(), activo, pageable)
                   .map(ProductoService::toDTO);
    }
    
    public List<Producto> findAll() {
    return repo.findAll();
}


}
