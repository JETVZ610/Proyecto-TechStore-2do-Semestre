package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.personas.Ciudad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class CiudadDAO {

    public Ciudad buscarPorId(int idCiudad) throws SQLException {

        String sql = "SELECT ID_Ciudad, Nombre_Ciudad, Provincia "
                + "FROM CIUDAD WHERE ID_Ciudad = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCiudad);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Ciudad(
                            rs.getInt("ID_Ciudad"),
                            rs.getString("Nombre_Ciudad"),
                            rs.getString("Provincia"));}}
        }
        return null;
    }

    public List<Ciudad> listar() throws SQLException {

        List<Ciudad> ciudades = new ArrayList<>();

        String sql = "SELECT ID_Ciudad, Nombre_Ciudad, Provincia "
                + "FROM CIUDAD ORDER BY Nombre_Ciudad";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Ciudad ciudad = new Ciudad(
                        rs.getInt("ID_Ciudad"),
                        rs.getString("Nombre_Ciudad"),
                        rs.getString("Provincia")
                );

                ciudades.add(ciudad);
            }
        }

        return ciudades;
    }

    public Ciudad insertar(String nombreCiudad, String provincia) throws SQLException {
        if (nombreCiudad == null || nombreCiudad.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la ciudad no puede estar vacío.");}
        if (provincia == null || provincia.isBlank()) {
            throw new IllegalArgumentException(
                    "La provincia no puede estar vacía.");}

        String sql = "INSERT INTO CIUDAD (Nombre_Ciudad, Provincia) "
                + "VALUES (?, ?)";

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);

            try {
                Ciudad ciudad;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, nombreCiudad);
                    ps.setString(2, provincia);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo insertar la ciudad.");}

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo recuperar el ID generado.");}

                        ciudad = new Ciudad(
                                rs.getInt(1),
                                nombreCiudad,
                                provincia
                        );
                    }
                }

                con.commit();
                return ciudad;

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

    public boolean actualizar(int idCiudad, String nombreCiudad, String provincia) throws SQLException {

        if (nombreCiudad == null || nombreCiudad.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la ciudad no puede estar vacío.");}
        if (provincia == null || provincia.isBlank()) {
            throw new IllegalArgumentException(
                    "La provincia no puede estar vacía.");}

        String sql = "UPDATE CIUDAD "
                + "SET Nombre_Ciudad = ?, Provincia = ? "
                + "WHERE ID_Ciudad = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreCiudad);
            ps.setString(2, provincia);
            ps.setInt(3, idCiudad);

            return ps.executeUpdate() == 1;
        }
    }
}