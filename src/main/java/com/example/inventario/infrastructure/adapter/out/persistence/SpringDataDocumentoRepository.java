package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataDocumentoRepository extends JpaRepository<DocumentoEntity, Integer> {
    List<DocumentoEntity> findAllByOrderByTipoAsc();
}
