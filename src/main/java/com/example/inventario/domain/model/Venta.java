package com.example.inventario.domain.model;

public class Venta{ //deberia referencias detalleVentas pero no m quiero adelantar a los hechos
    private Long id;
    private String fecha;
    private double total;
    private String estado;

    public double calcularTotal(){
        return 0.0;
    }

    public void confirmar(){

    }
}
