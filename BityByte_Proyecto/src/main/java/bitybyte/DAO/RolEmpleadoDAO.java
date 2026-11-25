package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.personas.RolEmpleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class RolEmpleadoDAO {

    private static final String SELECT_BASE =
            "SELECT ID_Rol, Nombre_Rol, Descripcion "
                    + "FROM ROL_EMPLEADO";

    public RolEmpleado buscarPorId(int idRol) throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            return buscarPorId(con, idRol);
        }
    }

    RolEmpleado buscarPorId(Connection con, int idRol) throws SQLException {
        if (idRol <= 0) {

            throw new IllegalArgumentException("El ID del rol debe ser positivo.");}

        String sql = SELECT_BASE + " WHERE ID_Rol = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() ? construirRol(rs) : null;
            }
        }
    }

    public RolEmpleado buscarPorNombre(String nombre) throws SQLException {

        if (nombre == null || nombre.isBlank()
                || nombre.length() > 80) {

            throw new IllegalArgumentException("El nombre del rol debe contener entre 1 y 80 caracteres.");}

        String sql = SELECT_BASE + " WHERE Nombre_Rol = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setNString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() ? construirRol(rs) : null;
            }
        }
    }

    public List<RolEmpleado> listar() throws SQLException {

        List<RolEmpleado> roles = new ArrayList<>();

        String sql = SELECT_BASE + " ORDER BY ID_Rol";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                roles.add(construirRol(rs));
            }
        }

        return roles;
    }

    private RolEmpleado construirRol(ResultSet rs) throws SQLException {

        return new RolEmpleado(
                rs.getInt("ID_Rol"),
                rs.getString("Nombre_Rol"),
                rs.getString("Descripcion")
        );
    }
}