package com.example.inventario.domain.model;

public class Rol {
    private String nombre;

    public Rol(String nombre) {
        this.nombre = nombre;
    }

    public boolean tienePermiso(String permiso) {
        if (nombre.equals("JEFE")) {
            return permiso.equals("GESTIONAR_USUARIOS")
                || permiso.equals("CONSULTAR_RENTABILIDAD")
                || permiso.equals("CONSULTAR_INVENTARIO");
        }

        if (nombre.equals("EMPLEADO")) {
            return permiso.equals("CONSULTAR_INVENTARIO");
        }

        return false;
    }
}