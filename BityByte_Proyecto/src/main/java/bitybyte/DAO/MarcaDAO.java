package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.inventario.Marca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class MarcaDAO {

    public Marca buscarPorId(int idMarca) throws SQLException {

        String sql = "SELECT ID_Marca, Nombre_Marca, Activo "
                + "FROM MARCA WHERE ID_Marca = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMarca);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return construirMarca(rs);
                }
            }
        }

        return null;
    }

    public List<Marca> listar() throws SQLException {

        String sql = "SELECT ID_Marca, Nombre_Marca, Activo "
                + "FROM MARCA ORDER BY Nombre_Marca";

        return ejecutarListado(sql);
    }

    public List<Marca> listarActivas() throws SQLException {

        String sql = "SELECT ID_Marca, Nombre_Marca, Activo "
                + "FROM MARCA WHERE Activo = 1 "
                + "ORDER BY Nombre_Marca";

        return ejecutarListado(sql);
    }

    public Marca insertar(String nombre) throws SQLException {

        validarNombre(nombre);

        String sql = "INSERT INTO MARCA (Nombre_Marca, Activo) "
                + "VALUES (?, 1)";

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {
                Marca marca;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, nombre);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo insertar la marca.");}

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo recuperar el ID generado.");}

                        marca = new Marca(
                                rs.getInt(1),
                                nombre,
                                true
                        );
                    }
                }

                con.commit();

                return marca;

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

    public boolean actualizar(int idMarca, String nombre)
            throws SQLException {

        validarNombre(nombre);

        String sql = "UPDATE MARCA "
                + "SET Nombre_Marca = ? "
                + "WHERE ID_Marca = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idMarca);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean activar(int idMarca) throws SQLException {

        return cambiarEstado(idMarca, true);
    }

    public boolean desactivar(int idMarca) throws SQLException {

        return cambiarEstado(idMarca, false);
    }

    private boolean cambiarEstado(int idMarca, boolean activo)
            throws SQLException {

        String sql = "UPDATE MARCA "
                + "SET Activo = ? "
                + "WHERE ID_Marca = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idMarca);

            return ps.executeUpdate() == 1;
        }
    }

    private Marca construirMarca(ResultSet rs)
            throws SQLException {

        return new Marca(
                rs.getInt("ID_Marca"),
                rs.getString("Nombre_Marca"),
                rs.getBoolean("Activo")
        );
    }

    private List<Marca> ejecutarListado(String sql)
            throws SQLException {

        List<Marca> marcas = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                marcas.add(construirMarca(rs));
            }
        }

        return marcas;
    }

    private void validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la marca no puede estar vacío.");}
        if (nombre.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres.");}
    }
}