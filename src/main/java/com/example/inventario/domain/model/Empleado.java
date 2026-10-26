package com.example.inventario.domain.model;

public class Empleado extends Usuario{
    private String cargo;

    public Empleado(int id, String nombre, String correo,
                    String contraseña, Rol rol, String cargo) {
        super(id, nombre, correo, contraseña, rol);
        this.cargo = cargo;
    }

    public void consultarInventario(){
        System.out.println("consultado");
    }
}