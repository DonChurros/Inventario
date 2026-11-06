package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCredencialUsuarioRepository
        extends JpaRepository<CredencialUsuarioEntity, Integer> {
}
