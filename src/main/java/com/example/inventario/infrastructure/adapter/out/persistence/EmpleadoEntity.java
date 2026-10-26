package com.example.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;


@Entity
@Table(name = "Empleados")

public class EmpleadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String apellido;

    public EmpleadoEntity() {}


    public EmpleadoEntity(int id, String nombre, String apellido) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public int id(){
        return id;
    }

    public String nombre(){
        return nombre;
    }

    public String apellido(){
        return apellido;
    }
}
