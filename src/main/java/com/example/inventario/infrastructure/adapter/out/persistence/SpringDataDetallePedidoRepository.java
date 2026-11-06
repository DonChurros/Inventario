package com.example.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataDetallePedidoRepository
        extends JpaRepository<DetallePedidoEntity, DetallePedidoId> {
    List<DetallePedidoEntity> findAllByPedidoIdOrderByProductoIdAsc(Integer pedidoId);
}
