package com.example.inventario.infrastructure.adapter.out.persistence;

import com.example.inventario.domain.model.Producto;
import com.example.inventario.application.port.out.ProductoRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class ProductoPersistenceAdapter
        implements ProductoRepositoryPort {
    private final SpringDataProductoRepository jpaRepository;
    public ProductoPersistenceAdapter(SpringDataProductoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Producto save(Producto producto) {
        ProductoEntity productoEntity = ProductoEntity.fromDomain(producto);
        jpaRepository.save(productoEntity);
        return productoEntity.toDomain();
    }

    // aqui pones mas cosas que añadas de salidas en el repository port


}
