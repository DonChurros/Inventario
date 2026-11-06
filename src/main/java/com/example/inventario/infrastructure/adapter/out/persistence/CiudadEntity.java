package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ciudades")
public class CiudadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CiudadId")
    private Integer id;

    @Column(name = "Ciudad", nullable = false, length = 255)
    private String ciudad;

    protected CiudadEntity() {
    }

    public Integer getId() {
        return id;
    }

    public String getCiudad() {
        return ciudad;
    }
}
