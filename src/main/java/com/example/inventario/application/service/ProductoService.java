package com.example.inventario.application.service;

import com.example.inventario.application.port.in.RegistrarProductoUseCase;
import com.example.inventario.application.port.out.ProductoRepositoryPort;
import com.example.inventario.domain.model.Producto;
import com.example.inventario.domain.model.OpcionCatalogo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements RegistrarProductoUseCase {
    private final ProductoRepositoryPort productoRepositoryPort;
    public ProductoService(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }
    @Override
    public Producto registrar(RegistrarCommand command){
        if (!productoRepositoryPort.categoriaExists(command.categoriaId())) {
            throw new IllegalArgumentException("La categoría seleccionada no existe.");
        }
        if (!productoRepositoryPort.empleadoExists(command.empleadoId())) {
            throw new IllegalArgumentException("El empleado seleccionado no existe.");
        }

        Producto producto = new Producto(0,
                command.categoriaId(),
                command.empleadoId(),
                command.nombre(),
                command.slug(),
                command.descripcion(),
                command.costo(),
                command.imagenUrl(),
                command.existencias(),
                command.destacado());
        return productoRepositoryPort.save(producto);
    }

    @Override
    public List<Producto> listar() {
        return productoRepositoryPort.findAll();
    }

    @Override
    public List<OpcionCatalogo> obtenerCategorias() {
        return productoRepositoryPort.findCategorias();
    }

    @Override
    public List<OpcionCatalogo> obtenerEmpleados() {
        return productoRepositoryPort.findEmpleados();
    }
}
