package com.example.inventario.application.port.in;

import com.example.inventario.domain.model.Empleado;
import com.example.inventario.domain.model.Rol;

public interface CrearEmpleadoUseCase {

    Empleado crear(crearCommand command);

    record crearCommand (
            int id,
            String nombre,
            String correo,
            String Contraseña,
            Rol rol

    ){}
}
