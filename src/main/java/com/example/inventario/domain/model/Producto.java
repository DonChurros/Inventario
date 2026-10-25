package com.example.inventario.domain.model;

public record Producto(
    int id,
    String nombre,
    String categoria,
    double precioUnitario       
        ){
    
    public Producto{
        if(precioUnitario < 0){
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
    }
}