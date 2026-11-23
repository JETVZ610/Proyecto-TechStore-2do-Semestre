package bitybyte.modelo.personas;

import bitybyte.interfaces.Autenticable;
import bitybyte.interfaces.Desactivable;
import bitybyte.modelo.ventas.Carrito;

public class ClienteOnline extends Cliente implements Desactivable, Autenticable {
    private String contrasena;
    private final Carrito carrito;
    private boolean activo;

    public ClienteOnline(Persona persona, String contrasena, Carrito carrito, boolean activo) {
        super(persona);

        if(contrasena == null || contrasena.isBlank()){
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");}

        if (carrito == null) {
            throw new IllegalArgumentException("El ClienteOnline debe tener un carrito.");}

        this.contrasena = contrasena;
        this.carrito = carrito;
        this.activo = activo;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public boolean puedeComprarEnLinea() {
        return activo;
    }

    @Override
    public boolean validarCredenciales(String contrasena){return activo && this.contrasena.equals(contrasena);}

    @Override
    public boolean cambiarContrasena(String actual, String nueva){
        if(!activo || nueva == null || nueva.isBlank()){
            return false;}

        if(this.contrasena.equals(actual)){
            this.contrasena = nueva;
            return true;}

        return false;
    }

    @Override
    public boolean estaActivo() {return activo;}

    @Override
    public void activar() {this.activo = true;}

    @Override
    public void desactivar() {this.activo = false;}
}