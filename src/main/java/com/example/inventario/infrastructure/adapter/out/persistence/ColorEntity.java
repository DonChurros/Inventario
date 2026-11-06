package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Colores")
public class ColorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ColorId")
    private Integer id;

    @Column(name = "ProductoId", nullable = false)
    private Integer productoId;

    @Column(name = "Nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "CodigoHex", nullable = false, length = 7)
    private String codigoHex = "#14B8A6";

    protected ColorEntity() {
    }

    public ColorEntity(Integer productoId, String nombre) {
        this(productoId, nombre, "#14B8A6");
    }

    public ColorEntity(Integer productoId, String nombre, String codigoHex) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.codigoHex = codigoHex;
    }

    public Integer getId() {
        return id;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoHex() {
        return codigoHex;
    }
}
