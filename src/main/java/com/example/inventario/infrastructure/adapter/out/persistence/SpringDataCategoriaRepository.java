package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCategoriaRepository extends JpaRepository<CategoriaEntity, Integer> {
    List<CategoriaEntity> findAllByOrderByNombreAsc();

    List<CategoriaEntity> findAllByEstadoIgnoreCaseOrderByNombreAsc(String estado);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByIdAndEstadoIgnoreCase(Integer id, String estado);
}
