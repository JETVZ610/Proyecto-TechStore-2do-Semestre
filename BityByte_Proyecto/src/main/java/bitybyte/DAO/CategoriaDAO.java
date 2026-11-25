package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.inventario.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public Categoria buscarPorId(int idCategoria) throws SQLException {

        String sql = "SELECT ID_Categoria, Nombre_Categoria, Activo "
                + "FROM CATEGORIA WHERE ID_Categoria = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return construirCategoria(rs);
                }
            }
        }

        return null;
    }

    public List<Categoria> listar() throws SQLException {

        String sql = "SELECT ID_Categoria, Nombre_Categoria, Activo "
                + "FROM CATEGORIA ORDER BY Nombre_Categoria";

        return ejecutarListado(sql);
    }

    public List<Categoria> listarActivas() throws SQLException {

        String sql = "SELECT ID_Categoria, Nombre_Categoria, Activo "
                + "FROM CATEGORIA WHERE Activo = 1 "
                + "ORDER BY Nombre_Categoria";

        return ejecutarListado(sql);
    }

    public Categoria insertar(String nombre) throws SQLException {

        validarNombre(nombre);

        String sql = "INSERT INTO CATEGORIA (Nombre_Categoria, Activo) "
                + "VALUES (?, 1)";

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {
                Categoria categoria;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, nombre);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo insertar la categoría.");
                    }

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo recuperar el ID generado.");
                        }

                        categoria = new Categoria(
                                rs.getInt(1),
                                nombre,
                                true
                        );
                    }
                }

                con.commit();

                return categoria;

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

    public boolean actualizar(int idCategoria, String nombre)
            throws SQLException {

        validarNombre(nombre);

        String sql = "UPDATE CATEGORIA "
                + "SET Nombre_Categoria = ? "
                + "WHERE ID_Categoria = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idCategoria);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean activar(int idCategoria) throws SQLException {return cambiarEstado(idCategoria, true);}
    public boolean desactivar(int idCategoria) throws SQLException {return cambiarEstado(idCategoria, false);}

    private boolean cambiarEstado(int idCategoria, boolean activo)
            throws SQLException {

        String sql = "UPDATE CATEGORIA "
                + "SET Activo = ? "
                + "WHERE ID_Categoria = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idCategoria);

            return ps.executeUpdate() == 1;
        }
    }

    private Categoria construirCategoria(ResultSet rs)
            throws SQLException {

        return new Categoria(
                rs.getInt("ID_Categoria"),
                rs.getString("Nombre_Categoria"),
                rs.getBoolean("Activo")
        );
    }

    private List<Categoria> ejecutarListado(String sql)
            throws SQLException {

        List<Categoria> categorias = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(construirCategoria(rs));
            }
        }

        return categorias;
    }

    private void validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría no puede estar vacío.");
        }

        if (nombre.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres.");
        }
    }
}