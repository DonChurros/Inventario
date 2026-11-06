package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataTallaRepository extends JpaRepository<TallaEntity, Integer> {
    List<TallaEntity> findAllByOrderBySistemaAscOrdenVisualAscNombreDescriptivoAsc();

    boolean existsByCodigoIgnoreCaseAndSistemaIgnoreCase(String codigo, String sistema);
}
