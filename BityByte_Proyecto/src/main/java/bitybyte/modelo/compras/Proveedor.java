package bitybyte.modelo.compras;

import bitybyte.interfaces.Desactivable;

import java.util.ArrayList;
import java.util.List;

public class Proveedor implements Desactivable {
    private final String ruc;
    private String razonSocial;
    private String correo;
    private String direccion;
    private final List<String> telefonos;
    private boolean activo;

    public Proveedor(String ruc, String razonSocial, String correo, String direccion,
                     List<String> telefonos, boolean activo){

        if(ruc == null || ruc.length() != 13){
            throw new IllegalArgumentException("El RUC debe tener 13 caracteres.");}
        if(razonSocial == null || razonSocial.isBlank()){
            throw new IllegalArgumentException("La razón social no puede estar vacía.");}
        if(correo == null || correo.isBlank()){
            throw new IllegalArgumentException("El correo no puede estar vacío.");}
        if(direccion == null || direccion.isBlank()){
            throw new IllegalArgumentException("La dirección no puede estar vacía.");}
        if(telefonos == null || telefonos.isEmpty()){
            throw new IllegalArgumentException("El proveedor debe tener al menos un teléfono.");}

        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.correo = correo;
        this.direccion = direccion;
        this.telefonos = new ArrayList<>();

        for(String telefono : telefonos){
            if(telefono == null || telefono.isBlank()){
                throw new IllegalArgumentException("Los teléfonos no pueden estar vacíos.");}
            if(this.telefonos.contains(telefono)){
                throw new IllegalArgumentException("No se pueden registrar teléfonos duplicados.");}

            this.telefonos.add(telefono);
        }
        this.activo = activo;
    }

    public String getRuc(){return ruc;}
    public String getRazonSocial(){return razonSocial;}
    public String getCorreo(){return correo;}
    public String getDireccion(){return direccion;}
    public List<String> getTelefonos(){return List.copyOf(telefonos);}

    public void actualizarDatos(String razonSocial, String correo, String direccion){
        if(razonSocial == null || razonSocial.isBlank()){
            return;}
        if(correo == null || correo.isBlank()){
            return;}
        if(direccion == null || direccion.isBlank()){
            return;}

        this.razonSocial = razonSocial;
        this.correo = correo;
        this.direccion = direccion;
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