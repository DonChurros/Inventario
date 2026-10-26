package com.example.inventario.application.port.in;

import com.example.inventario.domain.model.Jefe;
import com.example.inventario.domain.model.Rol;

public interface CrearJefeUseCase {
    Jefe crear(CrearJefeCommand command);

    record CrearJefeCommand (
            int id,
            String nombre,
            String correo,
            String contraseña,
            Rol rol
    ){}
}
