package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    List<UsuarioEntity> findAllByOrderByNombreAsc();

    boolean existsByCorreoIgnoreCase(String correo);
}
