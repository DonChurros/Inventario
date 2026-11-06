package com.example.inventario.application.port.out;

import com.example.inventario.domain.model.Producto;
import com.example.inventario.domain.model.OpcionCatalogo;

import java.util.List;

public interface ProductoRepositoryPort {
    Producto save(Producto producto);

    List<Producto> findAll();

    List<OpcionCatalogo> findCategorias();

    List<OpcionCatalogo> findEmpleados();

    boolean categoriaExists(int categoriaId);

    boolean empleadoExists(int empleadoId);
}
