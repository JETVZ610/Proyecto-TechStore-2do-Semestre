package bitybyte.modelo.personas;

public class Cliente{
    private final Persona persona;

    public Cliente(Persona persona){
        if(persona == null){
            throw new IllegalArgumentException("El cliente debe estar asociado a una persona.");}
        this.persona = persona;
    }

    public Persona getPersona(){return persona;}

}
