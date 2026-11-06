package com.example.inventario.infrastructure.adapter.in.web;

import com.example.inventario.application.port.in.RegistrarProductoUseCase;
import com.example.inventario.application.port.in.RegistrarProductoUseCase.RegistrarCommand;
import com.example.inventario.domain.model.OpcionCatalogo;
import com.example.inventario.domain.model.Producto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final RegistrarProductoUseCase registrarProductoUseCase;

    public ProductoController(RegistrarProductoUseCase registrarProductoUseCase) {
        this.registrarProductoUseCase = registrarProductoUseCase;
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return registrarProductoUseCase.listar();
    }

    @GetMapping("/opciones")
    public ProductOptions obtenerOpciones() {
        return new ProductOptions(
                registrarProductoUseCase.obtenerCategorias(),
                registrarProductoUseCase.obtenerEmpleados()
        );
    }

    @PostMapping
    public ResponseEntity<Producto> registrarProducto(@RequestBody CreateProductoRequest request) {
        Producto producto = registrarProductoUseCase.registrar(
                new RegistrarCommand(
                        request.categoriaId(),
                        request.empleadoId(),
                        request.nombre(),
                        request.slug(),
                        request.descripcion(),
                        request.costo(),
                        request.imagenUrl(),
                        request.existencias(),
                        request.destacado()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarDatosInvalidos(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }

    public record ProductOptions(List<OpcionCatalogo> categorias, List<OpcionCatalogo> empleados) {
    }

    public record CreateProductoRequest(
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

    public record ErrorResponse(String error) {
    }
}
