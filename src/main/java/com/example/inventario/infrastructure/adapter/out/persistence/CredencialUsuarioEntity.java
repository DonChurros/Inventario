package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CredencialesUsuarios")
public class CredencialUsuarioEntity {
    @Id
    @Column(name = "UsuarioId")
    private Integer usuarioId;

    @Column(name = "ContrasenaHash", nullable = false, length = 255)
    private String contrasenaHash;

    protected CredencialUsuarioEntity() {
    }

    public CredencialUsuarioEntity(Integer usuarioId, String contrasenaHash) {
        this.usuarioId = usuarioId;
        this.contrasenaHash = contrasenaHash;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }
}
