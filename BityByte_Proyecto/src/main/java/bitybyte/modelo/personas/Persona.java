package bitybyte.modelo.personas;

import java.util.ArrayList;
import java.util.List;

public class Persona {
    private final int idPersona;
    private final String cedula;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    private final List<String> telefonos;
    private Direccion direccion;

    public Persona(int idPersona, String cedula, String nombres, String apellidos,
                   String correoElectronico, List<String> telefonos, Direccion direccion){

        if(cedula == null || cedula.isBlank()){
            throw new IllegalArgumentException("La cédula no puede estar vacía.");}
        if(nombres == null || nombres.isBlank()){
            throw new IllegalArgumentException("Los nombres no pueden estar vacíos.");}
        if(apellidos == null || apellidos.isBlank()){
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos.");}
        if(correoElectronico == null || correoElectronico.isBlank()){
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío.");}
        if(telefonos == null || telefonos.isEmpty()){
            throw new IllegalArgumentException("La persona debe tener al menos un teléfono.");}
        if(direccion == null){
            throw new IllegalArgumentException("La persona debe tener una dirección principal.");}

        this.idPersona = idPersona;
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoElectronico = correoElectronico;
        this.telefonos = new ArrayList<>();

        for(String telefono : telefonos){
            if(telefono == null || telefono.isBlank()){
                throw new IllegalArgumentException("Los teléfonos no pueden estar vacíos.");}
            if(this.telefonos.contains(telefono)){
                throw new IllegalArgumentException("No se pueden registrar teléfonos duplicados.");}

            this.telefonos.add(telefono);
        }

        this.direccion = direccion;
    }

    public int getIdPersona(){return idPersona;}
    public String getCedula(){return cedula;}
    public String getNombres(){return nombres;}
    public String getApellidos(){return apellidos;}
    public String getCorreoElectronico(){return correoElectronico;}
    public List<String> getTelefonos(){return List.copyOf(telefonos);}
    public Direccion getDireccion(){return direccion;}

    public void actualizarDatos(String nombres, String apellidos, String correo){
        if(nombres == null || nombres.isBlank()){
            return;}
        if(apellidos == null || apellidos.isBlank()){
            return;}
        if(correo == null || correo.isBlank()){
            return;}

        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoElectronico = correo;
    }

    public void agregarTelefono(String telefono){
        if(telefono == null || telefono.isBlank()){
            return;
        }

        if(!telefonos.contains(telefono)){
            telefonos.add(telefono);
        }
    }

    public boolean eliminarTelefono(String telefono){
        if(telefono == null || telefonos.size() <= 1){
            return false;
        }

        return telefonos.remove(telefono);
    }

    public void actualizarDireccion(Direccion direccion){
        if(direccion == null){
            return;
        }

        this.direccion = direccion;
    }

    public String obtenerNombreCompleto(){return nombres + " " + apellidos;}
}