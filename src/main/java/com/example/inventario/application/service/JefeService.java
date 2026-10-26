package com.example.inventario.application.service;

import com.example.inventario.application.port.in.CrearJefeUseCase;
import com.example.inventario.application.port.in.CrearJefeUseCase.CrearJefeCommand;
import com.example.inventario.application.port.out.JefeRepositoryPort;
import com.example.inventario.domain.model.Jefe;
import org.springframework.stereotype.Service;


@Service
public class JefeService implements CrearJefeUseCase {
    private final JefeRepositoryPort jefeRepositoryPort;

    public JefeService(JefeRepositoryPort jefeRepositoryPort) {
        this.jefeRepositoryPort = jefeRepositoryPort;
    }

    @Override
    public Jefe crear(CrearJefeCommand command) {
        Jefe jefe = new Jefe(
                command.id(),
                command.nombre(),
                command.correo(),
                command.contraseña(),
                command.rol()
        );
        return jefeRepositoryPort.Save(jefe);
    }
}
