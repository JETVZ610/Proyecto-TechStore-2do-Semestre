package bitybyte.interfaces;

public interface Autenticable {
    public boolean validarCredenciales(String contrasena);
    public boolean cambiarContrasena(String actual, String nueva);
}
