package com.example.inventario.application.port.out;

import com.example.inventario.domain.model.Producto;

public interface ProductoRepositoryPort {
    Producto save(Producto producto);
}
