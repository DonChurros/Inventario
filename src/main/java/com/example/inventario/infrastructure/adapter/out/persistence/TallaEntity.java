package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CatalogoTallas")
public class TallaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TallaId")
    private Integer id;

    @Column(name = "Codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "NombreDescriptivo", nullable = false, length = 100)
    private String nombreDescriptivo;

    @Column(name = "Sistema", nullable = false, length = 30)
    private String sistema;

    @Column(name = "OrdenVisual", nullable = false)
    private int ordenVisual;

    @Column(name = "Estado", nullable = false, length = 20)
    private String estado = "Activo";

    protected TallaEntity() {
    }

    public TallaEntity(String codigo, String nombreDescriptivo, String sistema, int ordenVisual, String estado) {
        this.codigo = codigo;
        this.nombreDescriptivo = nombreDescriptivo;
        this.sistema = sistema;
        this.ordenVisual = ordenVisual;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombreDescriptivo() {
        return nombreDescriptivo;
    }

    public String getSistema() {
        return sistema;
    }

    public int getOrdenVisual() {
        return ordenVisual;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
