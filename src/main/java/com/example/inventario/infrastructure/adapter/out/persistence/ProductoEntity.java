package com.example.inventario.infrastructure.adapter.out.persistence;

import com.example.inventario.domain.model.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "Productos")
public class ProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductoId")
    private Integer id;

    @Column(name = "CategoriaId", nullable = false)
    private int categoriaId;

    @Column(name = "EmpleadoId", nullable = false)
    private int empleadoId;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "Descripcion")
    private String descripcion;

    @Column(name = "Costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "ImagenUrl", nullable = false, length = 255)
    private String imagenUrl;

    @Column(name = "Existencias", nullable = false)
    private int existencias;

    @Column(name = "Destacado", nullable = false)
    private boolean destacado;

    public ProductoEntity() {
    }

    public static ProductoEntity fromDomain(Producto producto) {
        ProductoEntity entity = new ProductoEntity();
        entity.categoriaId = producto.categoriaId();
        entity.empleadoId = producto.empleadoId();
        entity.nombre = producto.nombre();
        entity.slug = producto.slug();
        entity.descripcion = producto.descripcion();
        entity.costo = producto.costo();
        entity.imagenUrl = producto.imagenUrl();
        entity.existencias = producto.existencias();
        entity.destacado = producto.destacado();
        return entity;
    }

    public Producto toDomain() {
        return new Producto(
                id,
                categoriaId,
                empleadoId,
                nombre,
                slug,
                descripcion,
                costo,
                imagenUrl,
                existencias,
                destacado
        );
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public int getExistencias() {
        return existencias;
    }

    public void addExistencias(int cantidad) {
        this.existencias = Math.addExact(this.existencias, cantidad);
    }
}
