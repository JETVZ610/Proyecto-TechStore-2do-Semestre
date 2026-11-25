package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.personas.ClienteOnline;
import bitybyte.modelo.personas.Direccion;
import bitybyte.modelo.personas.Empleado;
import bitybyte.modelo.personas.Persona;
import bitybyte.modelo.ventas.Carrito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteOnlineDAO {

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final CarritoDAO carritoDAO = new CarritoDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    public ClienteOnline buscarPorId(int idPersona) throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {
            return buscarPorId(con, idPersona, false);
        }
    }

    ClienteOnline buscarPorId(Connection con, int idPersona, boolean bloquear) throws SQLException {

        validarId(idPersona);

        String sql = "SELECT Contrasena, Activo "
                + "FROM CLIENTE_ONLINE"
                + (bloquear ? " WITH (UPDLOCK, HOLDLOCK)" : "")
                + " WHERE ID_Persona = ?";

        String contrasena;
        boolean activo;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                contrasena = rs.getString("Contrasena");
                activo = rs.getBoolean("Activo");
            }
        }

        Persona persona = personaDAO.buscarPorId(
                con, idPersona
        );

        Carrito carrito = carritoDAO.buscarPorCliente(
                con, idPersona
        );

        if (persona == null || carrito == null) {

            throw new SQLException(
                    "La cuenta online tiene referencias inválidas.");
        }

        return new ClienteOnline(
                persona,
                contrasena,
                carrito,
                activo
        );
    }

    public ClienteOnline buscarPorCedula(String cedula) throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            Persona persona = personaDAO.buscarPorCedula(
                    con, cedula
            );

            if (persona == null) {
                return null;
            }

            return buscarPorId(
                    con, persona.getIdPersona(), false
            );
        }
    }

    public List<ClienteOnline> listar() throws SQLException {

        return ejecutarListado(false);
    }

    public List<ClienteOnline> listarActivos() throws SQLException {

        return ejecutarListado(true);
    }

    public ClienteOnline autenticar(String cedula, String contrasena) throws SQLException {

        ClienteOnline cliente = buscarPorCedula(cedula);

        if (cliente == null
                || !cliente.validarCredenciales(contrasena)) {

            return null;
        }

        return cliente;
    }

    public ClienteOnline insertar(
            String cedula,
            String nombres,
            String apellidos,
            String correo,
            List<String> telefonos,
            Direccion direccion,
            String contrasena,
            String confirmacion)
            throws SQLException {

        validarCedula(cedula);
        validarCorreo(correo);
        validarContrasena(contrasena);

        if (!contrasena.equals(confirmacion)) {
            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden.");
        }

        return ejecutarTransaccion(con -> {

            Persona persona = buscarPersonaParaRegistro(
                    con, cedula
            );

            if (persona == null) {

                persona = personaDAO.insertar(
                        con,
                        cedula,
                        nombres,
                        apellidos,
                        correo,
                        telefonos,
                        direccion
                );

            } else {

                if (!persona.getCorreoElectronico()
                        .equalsIgnoreCase(correo)) {

                    throw new IllegalArgumentException(
                            "La cédula y el correo electrónico "
                                    + "no corresponden a la misma persona.");
                }
            }

            int idPersona = persona.getIdPersona();

            if (buscarPorId(con, idPersona, true) != null) {

                throw new IllegalArgumentException(
                        "La persona ya dispone de una cuenta online.");
            }

            clienteDAO.insertar(con, persona);

            String sql = """
                    INSERT INTO CLIENTE_ONLINE (
                        ID_Persona, Contrasena, Activo
                    )
                    VALUES (?, ?, 1)
                    """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, idPersona);
                ps.setNString(2, contrasena);

                if (ps.executeUpdate() != 1) {

                    throw new SQLException(
                            "No se pudo registrar la cuenta online.");
                }
            }

            Carrito carrito = carritoDAO.insertar(
                    con, idPersona
            );

            return new ClienteOnline(
                    persona,
                    contrasena,
                    carrito,
                    true
            );
        });
    }

    public boolean cambiarContrasena(
            int idPersona,
            String actual,
            String nueva,
            String confirmacion) throws SQLException {

        validarId(idPersona);
        validarContrasena(nueva);

        if (!nueva.equals(confirmacion)) {
            return false;
        }

        return ejecutarTransaccion(con -> {

            ClienteOnline cliente = buscarPorId(
                    con, idPersona, true
            );

            if (cliente == null
                    || !cliente.cambiarContrasena(actual, nueva)) {

                return false;
            }

            return actualizarContrasena(
                    con, idPersona, nueva
            );
        });
    }

    public boolean recuperarContrasena(
            String cedula,
            String correo,
            String nueva,
            String confirmacion) throws SQLException {

        validarCedula(cedula);
        validarCorreo(correo);
        validarContrasena(nueva);

        if (!nueva.equals(confirmacion)) {
            return false;
        }

        String sql = """
                UPDATE co
                SET Contrasena = ?
                FROM CLIENTE_ONLINE co
                INNER JOIN PERSONA p
                    ON co.ID_Persona = p.ID_Persona
                WHERE p.Cedula = ?
                  AND p.Correo_Electronico = ?
                """;

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setNString(1, nueva);
            ps.setString(2, cedula);
            ps.setString(3, correo);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean activar(int idPersona, int idResponsable) throws SQLException {

        return cambiarEstado(idPersona, idResponsable, true);
    }

    public boolean desactivar(int idPersona, int idResponsable) throws SQLException {

        return cambiarEstado(
                idPersona, idResponsable, false
        );
    }

    private boolean cambiarEstado(int idPersona, int idResponsable, boolean activo) throws SQLException {

        validarId(idPersona);
        validarId(idResponsable);

        return ejecutarTransaccion(con -> {

            Empleado responsable = empleadoDAO.buscarPorId(
                    con, idResponsable, true
            );

            if (responsable == null
                    || !responsable.estaActivo()
                    || !responsable.tieneRol("Empleado Admin")) {

                throw new SecurityException(
                        "No tiene autorización para modificar "
                                + "el estado de la cuenta online.");
            }

            String sql = "UPDATE CLIENTE_ONLINE "
                    + "SET Activo = ? WHERE ID_Persona = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setBoolean(1, activo);
                ps.setInt(2, idPersona);

                return ps.executeUpdate() == 1;
            }
        });
    }

    private boolean actualizarContrasena(
            Connection con,
            int idPersona,
            String contrasena) throws SQLException {

        String sql = "UPDATE CLIENTE_ONLINE "
                + "SET Contrasena = ? WHERE ID_Persona = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setNString(1, contrasena);
            ps.setInt(2, idPersona);

            return ps.executeUpdate() == 1;
        }
    }

    private Persona buscarPersonaParaRegistro(Connection con, String cedula) throws SQLException {

        String sql = """
                SELECT ID_Persona
                FROM PERSONA WITH (UPDLOCK, HOLDLOCK)
                WHERE Cedula = ?
                """;

        int idPersona;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cedula);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                idPersona = rs.getInt("ID_Persona");
            }
        }

        Persona persona = personaDAO.buscarPorId(
                con, idPersona
        );

        if (persona == null) {

            throw new SQLException(
                    "No se pudieron recuperar los datos personales.");
        }

        return persona;
    }

    private List<ClienteOnline> ejecutarListado(boolean soloActivos) throws SQLException {

        List<ClienteOnline> clientes = new ArrayList<>();
        List<Integer> identificadores = new ArrayList<>();

        String sql = "SELECT ID_Persona FROM CLIENTE_ONLINE"
                + (soloActivos ? " WHERE Activo = 1" : "")
                + " ORDER BY ID_Persona";

        try (Connection con = Conexion.obtenerConexion()) {

            try (PreparedStatement ps =
                         con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    identificadores.add(
                            rs.getInt("ID_Persona")
                    );
                }
            }

            for (int id : identificadores) {

                ClienteOnline cliente = buscarPorId(
                        con, id, false
                );

                if (cliente != null
                        && (!soloActivos || cliente.estaActivo())) {

                    clientes.add(cliente);
                }
            }
        }

        return clientes;
    }

    @FunctionalInterface
    private interface OperacionSQL<T> {

        T ejecutar(Connection con) throws SQLException;
    }

    private <T> T ejecutarTransaccion(
            OperacionSQL<T> operacion)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {

                T resultado = operacion.ejecutar(con);

                con.commit();

                return resultado;

            } catch (SQLException | RuntimeException e) {

                try {
                    con.rollback();

                } catch (SQLException errorRollback) {

                    e.addSuppressed(errorRollback);
                }

                throw e;
            }
        }
    }

    private void validarId(int id) {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "El identificador debe ser positivo.");
        }
    }

    private void validarCedula(String cedula) {

        if (cedula == null || !cedula.matches("[0-9]{10}")) {

            throw new IllegalArgumentException(
                    "La cédula debe contener 10 dígitos.");
        }
    }

    private void validarCorreo(String correo) {

        if (correo == null || correo.isBlank()
                || correo.length() > 150) {

            throw new IllegalArgumentException(
                    "El correo debe contener entre 1 "
                            + "y 150 caracteres.");
        }
    }

    private void validarContrasena(String contrasena) {

        if (contrasena == null || contrasena.isBlank()
                || contrasena.length() > 255) {

            throw new IllegalArgumentException("La contraseña debe contener entre 1 " + "y 255 caracteres.");}
    }
}