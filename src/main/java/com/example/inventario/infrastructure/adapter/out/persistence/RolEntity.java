package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Roles")
public class RolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRol")
    private Integer id;

    @Column(name = "Rol", nullable = false, length = 50, unique = true)
    private String rol;

    protected RolEntity() {
    }

    public RolEntity(String rol) {
        this.rol = rol;
    }

    public Integer getId() {
        return id;
    }

    public String getRol() {
        return rol;
    }
}
