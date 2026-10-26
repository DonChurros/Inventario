package com.example.inventario.infrastructure.adapter.in.web;

import com.example.inventario.application.port.in.RegistrarProductoUseCase;
import com.example.inventario.application.port.in.RegistrarProductoUseCase.RegistrarCommand;
import com.example.inventario.domain.model.Producto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final RegistrarProductoUseCase registrarProductoUseCase;

    public ProductoController(RegistrarProductoUseCase registrarProductoUseCase) {
        this.registrarProductoUseCase = registrarProductoUseCase;
    }

    @PostMapping
    public ResponseEntity<Producto> registrarProducto(@RequestBody CreateProductoRequest request) {
        Producto producto = registrarProductoUseCase.registrar(
                new RegistrarCommand(
                        request.id(),
                        request.nombre(),
                        request.categoria(),
                        request.precioUnitario()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    public record CreateProductoRequest(
            int id,
            String nombre,
            String categoria,
            double precioUnitario
    ) {}
}
