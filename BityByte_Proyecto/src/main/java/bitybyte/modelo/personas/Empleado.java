package bitybyte.modelo.personas;

import bitybyte.enums.OperacionEmpleado;
import bitybyte.interfaces.Autenticable;
import bitybyte.interfaces.Desactivable;

public class Empleado implements Autenticable, Desactivable {
    private final String codEmpleado;
    private String contrasena;
    private boolean activo;
    private RolEmpleado rol;
    private final Persona persona;

    public Empleado(String codEmpleado, Persona persona, String contrasena,
                    boolean activo, RolEmpleado rol){

        if(codEmpleado == null || codEmpleado.isBlank()){
            throw new IllegalArgumentException("El código del empleado no puede estar vacío.");}
        if(persona == null){
            throw new IllegalArgumentException("El empleado debe estar asociado a una persona.");}
        if(contrasena == null || contrasena.isBlank()){
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");}
        if(rol == null){
            throw new IllegalArgumentException("El empleado debe tener un rol.");}

        this.codEmpleado = codEmpleado;
        this.persona = persona;
        this.contrasena = contrasena;
        this.activo = activo;
        this.rol = rol;
    }

    public String getCodEmpleado(){return codEmpleado;}
    public RolEmpleado getRol() {return rol;}
    public Persona getPersona(){return persona;}

    public boolean asignarRol(RolEmpleado nuevoRol, Empleado responsable){
        if(nuevoRol == null || responsable == null){
            return false;}
        if(!responsable.puedeRealizar(OperacionEmpleado.ASIGNAR_ROLES)){
            return false;}

        if(nuevoRol.getNombre().equals("Empleado Admin")){
            if(!responsable.tieneRol("Empleado Admin")){
                return false;}

            if(responsable == this){
                return false;}
        }

        this.rol = nuevoRol;
        return true;
    }

    public boolean tieneRol(String nombreRol){return this.rol.getNombre().equals(nombreRol);}

    public boolean puedeRealizar(OperacionEmpleado operacion){return activo && rol.permiteOperacion(operacion);}

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
    public boolean estaActivo(){return activo;}

    @Override
    public void activar(){this.activo = true;}

    @Override
    public void desactivar(){this.activo = false;}
}