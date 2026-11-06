package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataProductoTallaRepository extends JpaRepository<ProductoTallaEntity, Integer> {
    List<ProductoTallaEntity> findAllByProductoId(Integer productoId);

    List<ProductoTallaEntity> findAllByTallaId(Integer tallaId);

    boolean existsByProductoIdAndTallaId(Integer productoId, Integer tallaId);

    void deleteByProductoIdAndTallaId(Integer productoId, Integer tallaId);
}
