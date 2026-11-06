package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataColorRepository extends JpaRepository<ColorEntity, Integer> {
    List<ColorEntity> findAllByOrderByNombreAsc();

    List<ColorEntity> findAllByProductoIdOrderByNombreAsc(Integer productoId);

    boolean existsByProductoIdAndNombreIgnoreCase(Integer productoId, String nombre);
}
