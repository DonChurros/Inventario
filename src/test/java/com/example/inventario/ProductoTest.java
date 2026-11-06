package com.example.inventario;

import com.example.inventario.domain.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoTest {

    @Test
    void producto_respetarModeloDeBaseDeDatos() {
        Producto producto = new Producto(
                12,
                1,
                25,
                "Churro clásico",
                "churro-clasico",
                "Churro con azúcar",
                new BigDecimal("12.50"),
                "https://inventario.test/churro.jpg",
                7,
                false
        );

        assertEquals(12, producto.id());
        assertEquals(1, producto.categoriaId());
        assertEquals(25, producto.empleadoId());
        assertEquals(new BigDecimal("12.50"), producto.costo());
    }
}
