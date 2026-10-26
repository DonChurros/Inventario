package com.example.inventario.application.port.out;

import com.example.inventario.domain.model.Empleado;

public interface EmpleadoRepositoryPort {
    Empleado save(Empleado empleado);
}
