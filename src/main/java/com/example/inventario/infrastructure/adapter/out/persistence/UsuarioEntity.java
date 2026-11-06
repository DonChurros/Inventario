package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Usuarios")
public class UsuarioEntity {
    @Id
    @Column(name = "UsuarioId")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DocumentoId", nullable = false)
    private Integer documentoId;

    @Column(name = "CiudadId", nullable = false)
    private Integer ciudadId;

    @Column(name = "Direccion", nullable = false, length = 150)
    private String direccion;

    @Column(name = "Telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "Correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "FechaNacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "FechaRegistro", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    protected UsuarioEntity() {
    }

    public UsuarioEntity(
            Integer id,
            Integer documentoId,
            Integer ciudadId,
            String nombre,
            String direccion,
            String telefono,
            String correo,
            LocalDate fechaNacimiento
    ) {
        this.id = id;
        this.documentoId = documentoId;
        this.ciudadId = ciudadId;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
        this.fechaNacimiento = fechaNacimiento;
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

    public Integer getCiudadId() {
        return ciudadId;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
