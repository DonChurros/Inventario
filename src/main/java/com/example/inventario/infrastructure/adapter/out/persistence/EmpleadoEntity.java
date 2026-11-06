package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Empleados")
public class EmpleadoEntity {
    @Id
    @Column(name = "EmpleadoId")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DocumentoId", nullable = false)
    private Integer documentoId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "IdRol", nullable = false)
    private RolEntity rol;

    @Column(name = "Telefono", length = 20)
    private String telefono;

    @Column(name = "Correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "ContrasenaHash", length = 255)
    private String contrasenaHash;

    @Column(name = "Estado", nullable = false, length = 20)
    private String estado;

    protected EmpleadoEntity() {
    }

    public EmpleadoEntity(Integer id, String nombre, RolEntity rol) {
        this(id, 1, nombre, rol, null, "empleado-" + id + "@inventario.test", null, "Activo");
    }

    public EmpleadoEntity(
            Integer id,
            Integer documentoId,
            String nombre,
            RolEntity rol,
            String telefono,
            String correo
    ) {
        this(id, documentoId, nombre, rol, telefono, correo, null, "Activo");
    }

    public EmpleadoEntity(
            Integer id,
            Integer documentoId,
            String nombre,
            RolEntity rol,
            String telefono,
            String correo,
            String contrasenaHash,
            String estado
    ) {
        this.id = id;
        this.documentoId = documentoId;
        this.nombre = nombre;
        this.rol = rol;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getDocumentoId() {
        return documentoId;
    }

    public String getRol() {
        return rol.getRol();
    }

    public Integer getRolId() {
        return rol.getId();
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public String getEstado() {
        return estado;
    }
}
