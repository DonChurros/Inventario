package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "Pedidos")
public class PedidoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PedidoId")
    private Integer id;

    @Column(name = "UsuarioId")
    private Integer usuarioId;

    @Column(name = "FechaCreacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "Estado", length = 50)
    private String estado;

    protected PedidoEntity() {
    }

    public PedidoEntity(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void establecerEstadoInicial() {
        if (estado == null || estado.isBlank()) {
            estado = "Generado";
        }
    }

    public Integer getId() {
        return id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }
}
