package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCiudadRepository extends JpaRepository<CiudadEntity, Integer> {
    List<CiudadEntity> findAllByOrderByCiudadAsc();
}
