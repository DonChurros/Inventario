package com.example.inventario.infrastructure.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;

public class DetallePedidoId implements Serializable {
    private Integer pedidoId;
    private Integer productoId;

    public DetallePedidoId() {
    }

    public DetallePedidoId(Integer pedidoId, Integer productoId) {
        this.pedidoId = pedidoId;
        this.productoId = productoId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DetallePedidoId that)) return false;
        return Objects.equals(pedidoId, that.pedidoId)
                && Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedidoId, productoId);
    }
}
