package com.example.inventario.infrastructure.adapter.out.persistence;

import com.example.inventario.domain.model.Producto;
import com.example.inventario.application.port.out.ProductoRepositoryPort;
import com.example.inventario.domain.model.OpcionCatalogo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductoPersistenceAdapter
        implements ProductoRepositoryPort {
    private final SpringDataProductoRepository jpaRepository;
    private final SpringDataCategoriaRepository categoriaRepository;
    private final SpringDataEmpleadoRepository empleadoRepository;

    public ProductoPersistenceAdapter(
            SpringDataProductoRepository jpaRepository,
            SpringDataCategoriaRepository categoriaRepository,
            SpringDataEmpleadoRepository empleadoRepository
    ) {
        this.jpaRepository = jpaRepository;
        this.categoriaRepository = categoriaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public Producto save(Producto producto) {
        ProductoEntity productoEntity = ProductoEntity.fromDomain(producto);
        return jpaRepository.save(productoEntity).toDomain();
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAll().stream()
                .map(ProductoEntity::toDomain)
                .toList();
    }

    @Override
    public List<OpcionCatalogo> findCategorias() {
        return categoriaRepository.findAllByEstadoIgnoreCaseOrderByNombreAsc("Activo").stream()
                .map(categoria -> new OpcionCatalogo(categoria.getId(), categoria.getNombre()))
                .toList();
    }

    @Override
    public List<OpcionCatalogo> findEmpleados() {
        return empleadoRepository.findAllByOrderByNombreAsc().stream()
                .map(empleado -> new OpcionCatalogo(empleado.getId(), empleado.getNombre()))
                .toList();
    }

    @Override
    public boolean categoriaExists(int categoriaId) {
        return categoriaRepository.existsByIdAndEstadoIgnoreCase(categoriaId, "Activo");
    }

    @Override
    public boolean empleadoExists(int empleadoId) {
        return empleadoRepository.existsById(empleadoId);
    }
}
