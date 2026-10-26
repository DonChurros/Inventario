package com.example.inventario.application.service;

import com.example.inventario.application.port.in.CrearEmpleadoUseCase;
import com.example.inventario.application.port.out.EmpleadoRepositoryPort;
import com.example.inventario.domain.model.Empleado;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoService implements CrearEmpleadoUseCase {

    private final EmpleadoRepositoryPort empleadoRepositoryPort;
    public EmpleadoService(EmpleadoRepositoryPort empleadoRepositoryPort) {
        this.empleadoRepositoryPort = empleadoRepositoryPort;
    }

    @Override
    public Empleado crear(crearCommand command) {
        Empleado empleado = new Empleado(
                command.id(),
                command.nombre(),
                command.correo(),
                command.Contraseña(),
                command.rol());
        return empleadoRepositoryPort.save(empleado);
    }
}
