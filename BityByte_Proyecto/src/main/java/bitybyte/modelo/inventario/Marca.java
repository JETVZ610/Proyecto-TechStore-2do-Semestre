package bitybyte.modelo.inventario;

import bitybyte.interfaces.Desactivable;

public class Marca implements Desactivable {
    private final int idMarca;
    private String nombre;
    private boolean activo;

    public Marca(int idMarca, String nombre, boolean activo){
        this.idMarca = idMarca;
        this.nombre = nombre;
        this.activo = activo;
    }

    public int getIdMarca(){return idMarca;}
    public String getNombre(){return nombre;}

    public void actualizarNombre(String nombre){this.nombre = nombre;}

    @Override
    public boolean estaActivo(){
        return activo;
    }

    @Override
    public void activar(){
        this.activo = true;
    }

    @Override
    public void desactivar(){
        this.activo = false;
    }
}
