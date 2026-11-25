package bitybyte.modelo.inventario;

import bitybyte.interfaces.Desactivable;

public class Categoria implements Desactivable {
    private final int idCategoria;
    private String nombre;
    private boolean activo;

    public Categoria(int idCategoria, String nombre, boolean activo){
        validarNombre(nombre);

        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.activo = activo;
    }

    public int getIdCategoria(){return idCategoria;}
    public String getNombre(){return nombre;}

    public void actualizarNombre(String nombre){
        validarNombre(nombre);
        this.nombre = nombre;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría no puede estar vacío.");}
        if (nombre.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres.");}
    }

    @Override
    public boolean estaActivo(){return activo;}

    @Override
    public void activar(){this.activo = true;}

    @Override
    public void desactivar(){this.activo = false;}
}