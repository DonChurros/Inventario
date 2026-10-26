package com.example.inventario.application.port.out;

import com.example.inventario.domain.model.Jefe;

public interface JefeRepositoryPort {
    Jefe Save(Jefe jefe);
}
