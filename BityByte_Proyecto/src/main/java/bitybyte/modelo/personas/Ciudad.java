package bitybyte.modelo.personas;

public class Ciudad {
    private final int idCiudad;
    private final String nombreCiudad;
    private final String provincia;

    public Ciudad(int idCiudad, String nombreCiudad, String provincia){
        if(nombreCiudad == null || nombreCiudad.isBlank()){
            throw new IllegalArgumentException("El nombre de la ciudad no puede estar vacío.");}
        if(provincia == null || provincia.isBlank()){
            throw new IllegalArgumentException("La provincia no puede estar vacía.");}

        this.idCiudad = idCiudad;
        this.nombreCiudad = nombreCiudad;
        this.provincia = provincia;
    }

    public int getIdCiudad(){return idCiudad;}
    public String getNombreCiudad(){return nombreCiudad;}
    public String getProvincia(){return provincia;}

    public String formatearUbicacion(){
        return getProvincia() + ", " + getNombreCiudad();
    }
}