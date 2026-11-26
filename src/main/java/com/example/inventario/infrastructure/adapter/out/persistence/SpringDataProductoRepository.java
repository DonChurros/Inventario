package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataProductoRepository
        extends JpaRepository<ProductoEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select producto from ProductoEntity producto where producto.id = :productoId")
    Optional<ProductoEntity> findByIdForUpdate(@Param("productoId") Integer productoId);
}
