package com.example.inventario.domain.model;

public class Jefe extends Usuario{

    public Jefe(int id, String nombre, String correo,
                String contraseña, Rol rol){
        super(id, nombre, correo, contraseña, rol);
    }

    public void gestionarUsuarios() {
        if (tienePermiso("GESTIONAR_USUARIOS")) {
            System.out.println("Gestionando usuarios...");
        } else {
            System.out.println("No tienes permiso");
        }
    }
}
