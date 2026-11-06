package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataInsumoMenorRepository extends JpaRepository<InsumoMenorEntity, Integer> {
    List<InsumoMenorEntity> findAllByOrderByNombreAsc();

    boolean existsByReferenciaIgnoreCase(String referencia);
}
