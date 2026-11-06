package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "ProductoTallas",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_ProductoTallas_Producto_Talla",
                columnNames = {"ProductoId", "TallaId"}
        )
)
public class ProductoTallaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductoTallaId")
    private Integer id;

    @Column(name = "ProductoId", nullable = false)
    private Integer productoId;

    @Column(name = "TallaId", nullable = false)
    private Integer tallaId;

    protected ProductoTallaEntity() {
    }

    public ProductoTallaEntity(Integer productoId, Integer tallaId) {
        this.productoId = productoId;
        this.tallaId = tallaId;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public Integer getTallaId() {
        return tallaId;
    }
}
