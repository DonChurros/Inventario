package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataPedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pedido from PedidoEntity pedido where pedido.id = :pedidoId")
    Optional<PedidoEntity> findByIdForUpdate(@Param("pedidoId") Integer pedidoId);
}
