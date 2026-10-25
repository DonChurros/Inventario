package com.example.inventario.domain.model;

public record Disponibilidad(
        Long productoId,
        boolean estaDisponible,
        int cantidadSolicitada,
        int stockDisponible,
        String mensaje
) {

    public Disponibilidad {
        if (mensaje == null) {
            mensaje = estaDisponible
                    ? "Stock suficiente"
                    : "Stock insuficiente. Disponible: " + stockDisponible;
        }
    }
}