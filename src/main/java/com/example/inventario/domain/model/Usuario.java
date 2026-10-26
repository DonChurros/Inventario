package com.example.inventario.domain.model;

public abstract class Usuario{
    public int id;
    private String nombre;
    private String correo;
    private String contraseña;
    private Boolean activo;
    private Rol rol;
    
    public Usuario(int id, String nombre, String correo, String contraseña, Rol rol){
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contraseña = contraseña;
        this.activo = false;
        this.rol = rol;
    }
    
    public boolean consultarEstado(){
        if(activo == null){
            System.out.println("de alguna manera es nulo");
            return false;
        }
        return activo;
    }
    
    public boolean tienePermiso(String permiso) {
        return rol.tienePermiso(permiso);
    }
    
    public boolean iniciarSesion(String correo, String contraseña){
        if (this.correo.equals(correo) && this.contraseña.equals(contraseña)){
            activo = true;
            return true;
        } else {
            System.out.println("no");
            return false;
        }
    }
    
    public void actualizarContraseña(String contraseña){
        this.contraseña = contraseña;
    }
}