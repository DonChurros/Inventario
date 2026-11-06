package com.example.inventario.domain.model;

import java.math.BigDecimal;

public record Producto(
        int id,
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
    public Producto {
        if (categoriaId <= 0 || empleadoId <= 0) {
            throw new IllegalArgumentException("Selecciona una categoría y un empleado existentes.");
        }
        if (nombre == null || nombre.isBlank() || nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre es obligatorio y debe tener máximo 100 caracteres.");
        }
        if (slug == null || slug.isBlank() || slug.length() > 100) {
            throw new IllegalArgumentException("El slug es obligatorio y debe tener máximo 100 caracteres.");
        }
        if (costo == null || costo.signum() < 0 || costo.scale() > 2 || costo.precision() > 10) {
            throw new IllegalArgumentException("El costo debe ser válido, no negativo y caber en DECIMAL(10,2).");
        }
        if (imagenUrl == null || imagenUrl.isBlank() || imagenUrl.length() > 255) {
            throw new IllegalArgumentException("La URL de imagen es obligatoria y debe tener máximo 255 caracteres.");
        }
        if (existencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }
    }
}
