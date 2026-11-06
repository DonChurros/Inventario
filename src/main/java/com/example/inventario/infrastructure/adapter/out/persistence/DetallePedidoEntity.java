package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "DetallePedido")
@IdClass(DetallePedidoId.class)
public class DetallePedidoEntity {
    @Id
    @Column(name = "PedidoId", nullable = false)
    private Integer pedidoId;

    @Id
    @Column(name = "ProductoId", nullable = false)
    private Integer productoId;

    @Column(name = "Cantidad", nullable = false)
    private int cantidad;

    @Column(name = "Costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    protected DetallePedidoEntity() {
    }

    public DetallePedidoEntity(Integer pedidoId, Integer productoId, int cantidad, BigDecimal costo) {
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.costo = costo;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void addCantidad(int cantidad) {
        this.cantidad = Math.addExact(this.cantidad, cantidad);
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
