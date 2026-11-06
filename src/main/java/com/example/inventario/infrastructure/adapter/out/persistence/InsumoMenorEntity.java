package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "InsumosMenores")
public class InsumoMenorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InsumoId")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Referencia", nullable = false, length = 50)
    private String referencia;

    @Column(name = "UnidadMedida", nullable = false, length = 30)
    private String unidadMedida;

    @Column(name = "CostoUnitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "CantidadDisponible", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadDisponible;

    protected InsumoMenorEntity() {
    }

    public InsumoMenorEntity(
            String nombre,
            String referencia,
            String unidadMedida,
            BigDecimal costoUnitario,
            BigDecimal cantidadDisponible
    ) {
        this.nombre = nombre;
        this.referencia = referencia;
        this.unidadMedida = unidadMedida;
        this.costoUnitario = costoUnitario;
        this.cantidadDisponible = cantidadDisponible;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public BigDecimal getCantidadDisponible() {
        return cantidadDisponible;
    }
}
