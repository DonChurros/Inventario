package com.example.inventario.application.port.in;

import com.example.inventario.domain.model.Producto;

public interface RegistrarProductoUseCase {
    Producto registrar(RegistrarCommand command);

    record RegistrarCommand (
            int id,
            String nombre,
            String categoria,
            double precioUnitario
    ){
        public RegistrarCommand{
             if (precioUnitario < 0){
                 throw new IllegalArgumentException("El precio no puede ser negativo");
             }
        }
    }
}
