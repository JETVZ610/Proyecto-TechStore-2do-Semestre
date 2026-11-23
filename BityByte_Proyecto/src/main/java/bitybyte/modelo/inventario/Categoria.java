package bitybyte.modelo.inventario;

import bitybyte.interfaces.Desactivable;

public class Categoria implements Desactivable {
    private final int idCategoria;
    private String nombre;
    private boolean activo;

    public Categoria(int idCategoria, String nombre, boolean activo){
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.activo = activo;
    }

    public int getIdCategoria(){return idCategoria;}
    public String getNombre(){return nombre;}

    public void actualizarNombre(String nombre){this.nombre = nombre;}

    @Override
    public boolean estaActivo(){return activo;}

    @Override
    public void activar(){this.activo = true;}

    @Override
    public void desactivar(){this.activo = false;}
}