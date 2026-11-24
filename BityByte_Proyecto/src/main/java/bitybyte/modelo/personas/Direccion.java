package bitybyte.modelo.personas;

public class Direccion {
    private final Ciudad ciudad;
    private final String callePrincipal;
    private final String calleSecundaria;
    private final String referencia;

    public Direccion(Ciudad ciudad, String callePrincipal, String calleSecundaria, String referencia){
        if(ciudad == null){
            throw new IllegalArgumentException("La dirección debe tener una ciudad.");}
        if(callePrincipal == null || callePrincipal.isBlank()){
            throw new IllegalArgumentException("La calle principal no puede estar vacía.");}

        this.ciudad = ciudad;
        this.callePrincipal = callePrincipal;
        this.calleSecundaria = calleSecundaria;
        this.referencia = referencia;
    }

    public Ciudad getCiudad(){return ciudad;}
    public String getCallePrincipal(){return callePrincipal;}
    public String getCalleSecundaria(){return calleSecundaria;}
    public String getReferencia(){return referencia;}

    public String formatearDireccion(){
        String direccion = ciudad.formatearUbicacion() + ", " + getCallePrincipal();

        if(calleSecundaria != null && !calleSecundaria.isBlank()){
            direccion += ", " + calleSecundaria;}

        if(referencia != null && !referencia.isBlank()){
            direccion += ", " + referencia;}

        return direccion;
    }
}
