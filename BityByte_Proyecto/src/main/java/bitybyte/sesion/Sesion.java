package bitybyte.sesion;

import bitybyte.enums.OperacionEmpleado;
import bitybyte.modelo.personas.ClienteOnline;
import bitybyte.modelo.personas.Empleado;
import bitybyte.modelo.personas.Persona;
import bitybyte.modelo.personas.RolEmpleado;

public final class Sesion {

    public enum TipoAcceso {
        CLIENTE_ONLINE,
        EMPLEADO
    }

    private final TipoAcceso tipoAcceso;
    private final int idPersona;
    private final String nombreCompleto;
    private final Integer idCarrito;
    private final RolEmpleado rol;

    private Sesion(TipoAcceso tipoAcceso, Persona persona, Integer idCarrito, RolEmpleado rol) {
        this.tipoAcceso = tipoAcceso;
        this.idPersona = persona.getIdPersona();
        this.nombreCompleto = persona.obtenerNombreCompleto();
        this.idCarrito = idCarrito;
        this.rol = rol;
    }

    public static Sesion paraClienteOnline(ClienteOnline cliente) {

        if (cliente == null || !cliente.estaActivo()) {
            throw new IllegalArgumentException(
                    "Se requiere un cliente online activo.");}

        return new Sesion(
                TipoAcceso.CLIENTE_ONLINE,
                cliente.getPersona(),
                cliente.getCarrito().getIdCarrito(),
                null
        );
    }

    public static Sesion paraEmpleado(Empleado empleado) {

        if (empleado == null || !empleado.estaActivo()) {
            throw new IllegalArgumentException(
                    "Se requiere un empleado activo.");}

        return new Sesion(
                TipoAcceso.EMPLEADO,
                empleado.getPersona(),
                null,
                empleado.getRol()
        );
    }

    public TipoAcceso getTipoAcceso() {return tipoAcceso;}
    public int getIdPersona() {return idPersona;}
    public String getNombreCompleto() {return nombreCompleto;}

    public boolean esClienteOnline() {
        return tipoAcceso == TipoAcceso.CLIENTE_ONLINE;
    }

    public boolean esEmpleado() {
        return tipoAcceso == TipoAcceso.EMPLEADO;
    }

    public boolean esAdministrador() {
        return esEmpleado() && "Empleado Admin".equals(rol.getNombre());
    }

    public int getIdCarrito() {
        if (!esClienteOnline()) {
            throw new IllegalStateException(
                    "La sesión no corresponde a un cliente online.");}

        return idCarrito;
    }

    public RolEmpleado getRol() {
        if (!esEmpleado()) {
            throw new IllegalStateException(
                    "La sesión no corresponde a un empleado.");
        }

        return rol;
    }

    public boolean puedeRealizar(OperacionEmpleado operacion) {
        return esEmpleado()
                && rol.permiteOperacion(operacion);
    }
}