package com.example.inventario.domain.model;

public class Insumo{
    private Long id;
    private String fechaInicio;
    private String fechaFin;
    private int cantidadProductos;
    private double costoTotal;

    public double calcularCostoTotal(){
        return 0.0;
    }

    public void cerraLote(){

    }
}
