package com.example.inventario.application.port.in;

import com.example.inventario.domain.model.Disponibilidad;

public interface ObtenerDisponibilidadUseCase {
    Disponibilidad ejecutar(Long productoId, int cantidadSolicitada);
}
