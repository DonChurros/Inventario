package com.example.inventario.application.port.in;

import com.example.inventario.domain.model.Producto;
import com.example.inventario.domain.model.OpcionCatalogo;

import java.math.BigDecimal;
import java.util.List;

public interface RegistrarProductoUseCase {
    Producto registrar(RegistrarCommand command);

    List<Producto> listar();

    List<OpcionCatalogo> obtenerCategorias();

    List<OpcionCatalogo> obtenerEmpleados();

    record RegistrarCommand(
            int categoriaId,
            int empleadoId,
            String nombre,
            String slug,
            String descripcion,
            BigDecimal costo,
            String imagenUrl,
            int existencias,
            boolean destacado
    ) {
    }
}
