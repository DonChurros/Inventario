package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataRolRepository extends JpaRepository<RolEntity, Integer> {
    List<RolEntity> findAllByOrderByRolAsc();
}
