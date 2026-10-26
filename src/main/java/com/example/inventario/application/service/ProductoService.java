package com.example.inventario.application.service;

import com.example.inventario.application.port.in.RegistrarProductoUseCase;
import com.example.inventario.application.port.out.ProductoRepositoryPort;
import com.example.inventario.domain.model.Producto;
import org.springframework.stereotype.Service;

@Service
public class ProductoService implements RegistrarProductoUseCase {
    private final ProductoRepositoryPort productoRepositoryPort;
    public ProductoService(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }
    @Override
    public Producto registrar(RegistrarCommand command){
        Producto producto = new Producto(command.id(),
                command.nombre(),
                command.categoria(),
                command.precioUnitario());
        return productoRepositoryPort.save(producto);
    }
}

