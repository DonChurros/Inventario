package com.example.inventario.infrastructure.adapter.out.persistence;


import com.example.inventario.domain.model.Producto;
import jakarta.persistence.*;

@Entity
@Table(name = "Productos")
public class ProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String categoria;
    private double precioUnitario;

    public ProductoEntity() {};

    public static ProductoEntity fromDomain(Producto producto){
        ProductoEntity productoEntity = new ProductoEntity();
        if (producto.id() != 0) {
            productoEntity.id = producto.id();
        }
        productoEntity.nombre = producto.nombre();
        productoEntity.categoria = producto.categoria();
        productoEntity.precioUnitario = producto.precioUnitario();
        return productoEntity;
    }

    public Producto toDomain(){
        return new Producto(this.id,
                this.nombre,
                this.categoria,
                this.precioUnitario);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

}
