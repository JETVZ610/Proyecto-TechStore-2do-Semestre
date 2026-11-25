package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.personas.Cliente;
import bitybyte.modelo.personas.Persona;
import bitybyte.modelo.personas.Direccion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private final PersonaDAO personaDAO = new PersonaDAO();

    public Cliente buscarPorId(int idPersona) throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {
            return buscarPorId(con, idPersona);
        }
    }

    Cliente buscarPorId(Connection con, int idPersona) throws SQLException {

        if (idPersona <= 0) {
            throw new IllegalArgumentException("El ID de la persona debe ser positivo.");}

        String sql = "SELECT ID_Persona FROM CLIENTE "
                + "WHERE ID_Persona = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }
            }
        }

        Persona persona = personaDAO.buscarPorId(con, idPersona);

        if (persona == null) {
            throw new SQLException(
                    "No se encontraron los datos del cliente.");
        }

        return new Cliente(persona);
    }

    public Cliente buscarPorCedula(String cedula) throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            Persona persona = personaDAO.buscarPorCedula(
                    con, cedula
            );

            if (persona == null) {
                return null;
            }

            return buscarPorId(con, persona.getIdPersona());
        }
    }

    public List<Cliente> listar() throws SQLException {

        List<Cliente> clientes = new ArrayList<>();
        List<Integer> identificadores = new ArrayList<>();

        String sql = "SELECT ID_Persona FROM CLIENTE "
                + "ORDER BY ID_Persona";

        try (Connection con = Conexion.obtenerConexion()) {

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    identificadores.add(
                            rs.getInt("ID_Persona")
                    );
                }
            }

            for (int id : identificadores) {

                Cliente cliente = buscarPorId(con, id);

                if (cliente != null) {
                    clientes.add(cliente);
                }
            }
        }

        return clientes;
    }

    public Cliente insertar(String cedula, String nombres, String apellidos, String correo, List<String> telefonos, Direccion direccion)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {
                Persona persona = personaDAO.buscarPorCedula(
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
                }

                Cliente cliente = insertar(con, persona);

                con.commit();

                return cliente;

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

    Cliente insertar(Connection con, Persona persona) throws SQLException {

        if (con.getAutoCommit()) {
            throw new SQLException("Se requiere una transacción compartida.");}

        if (persona == null || persona.getIdPersona() <= 0) {
            throw new IllegalArgumentException("Se requiere una persona registrada.");}

        String sqlConsulta = """
                SELECT ID_Persona
                FROM CLIENTE WITH (UPDLOCK, HOLDLOCK)
                WHERE ID_Persona = ?
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sqlConsulta)) {

            ps.setInt(1, persona.getIdPersona());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Cliente(persona);
                }
            }
        }

        String sql = "INSERT INTO CLIENTE (ID_Persona) "
                + "VALUES (?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, persona.getIdPersona());

            if (ps.executeUpdate() != 1) {

                throw new SQLException("No se pudo registrar el cliente.");}
        }

        return new Cliente(persona);
    }
}