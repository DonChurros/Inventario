package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "Categorias")
public class CategoriaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoriaId")
    private Integer id;

    @Column(name = "Nombre")
    private String nombre;

    @Column(name = "Slug", nullable = false, length = 250)
    private String slug;

    @Column(name = "Descripcion")
    private String descripcion;

    @Column(name = "ImagenUrl", nullable = false, length = 255)
    private String imagenUrl;

    @Column(name = "Estado", nullable = false, length = 20)
    private String estado = "Activo";

    @Column(name = "FechaCreacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    protected CategoriaEntity() {
    }

    public CategoriaEntity(String nombre) {
        this(nombre, nombre == null ? null : nombre.toLowerCase().replaceAll("[^a-z0-9]+", "-"),
                null, "https://inventario.test/categoria.jpg");
    }

    public CategoriaEntity(String nombre, String slug, String descripcion, String imagenUrl) {
        this(nombre, slug, descripcion, imagenUrl, "Activo");
    }

    public CategoriaEntity(String nombre, String slug, String descripcion, String imagenUrl, String estado) {
        this.nombre = nombre;
        this.slug = slug;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
