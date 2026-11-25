package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.compras.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    private static final String SELECT_BASE =
            "SELECT p.RUC, p.Razon_Social, p.Correo, "
                    + "p.Direccion, p.Activo, t.Telefono "
                    + "FROM PROVEEDOR p "
                    + "LEFT JOIN TELEFONO_PROVEEDOR t "
                    + "ON p.RUC = t.RUC_Proveedor ";

    private static final String ORDEN =
            "ORDER BY p.Razon_Social, p.RUC, t.Telefono";

    public Proveedor buscarPorRuc(String ruc) throws SQLException {

        validarRuc(ruc);

        String sql = SELECT_BASE
                + "WHERE p.RUC = ? " + ORDEN;

        List<Proveedor> proveedores = ejecutarListado(sql, ruc);

        return proveedores.isEmpty() ? null : proveedores.get(0);
    }

    public List<Proveedor> listar() throws SQLException {

        String sql = SELECT_BASE + ORDEN;

        return ejecutarListado(sql, null);
    }

    public List<Proveedor> listarActivos() throws SQLException {

        String sql = SELECT_BASE
                + "WHERE p.Activo = 1 " + ORDEN;

        return ejecutarListado(sql, null);
    }

    public Proveedor insertar(String ruc, String razonSocial, String correo, String direccion, List<String> telefonos) throws SQLException {
        Proveedor proveedor = validarDatos(
                ruc, razonSocial, correo, direccion, telefonos
        );

        String sql = "INSERT INTO PROVEEDOR "
                + "(RUC, Razon_Social, Correo, Direccion, Activo) "
                + "VALUES (?, ?, ?, ?, 1)";

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, proveedor.getRuc());
                    ps.setNString(2, proveedor.getRazonSocial());
                    ps.setString(3, proveedor.getCorreo());
                    ps.setNString(4, proveedor.getDireccion());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo insertar el proveedor.");
                    }
                }

                insertarTelefonos(
                        con,
                        proveedor.getRuc(),
                        proveedor.getTelefonos()
                );

                con.commit();

                return proveedor;

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

    public boolean actualizar(String ruc, String razonSocial, String correo, String direccion, List<String> telefonos) throws SQLException {
        Proveedor proveedor = validarDatos(
                ruc, razonSocial, correo, direccion, telefonos
        );

        String sql = "UPDATE PROVEEDOR "
                + "SET Razon_Social = ?, Correo = ?, Direccion = ? "
                + "WHERE RUC = ?";

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {

                int filasAfectadas;

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setNString(1, proveedor.getRazonSocial());
                    ps.setString(2, proveedor.getCorreo());
                    ps.setNString(3, proveedor.getDireccion());
                    ps.setString(4, proveedor.getRuc());

                    filasAfectadas = ps.executeUpdate();
                }

                if (filasAfectadas == 0) {
                    con.rollback();
                    return false;
                }

                if (filasAfectadas != 1) {
                    throw new SQLException(
                            "No se pudo actualizar el proveedor.");}

                eliminarTelefonos(con, proveedor.getRuc());

                insertarTelefonos(
                        con,
                        proveedor.getRuc(),
                        proveedor.getTelefonos()
                );

                con.commit();

                return true;

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

    public boolean activar(String ruc) throws SQLException {

        return cambiarEstado(ruc, true);
    }

    public boolean desactivar(String ruc) throws SQLException {

        return cambiarEstado(ruc, false);
    }

    private boolean cambiarEstado(String ruc, boolean activo)
            throws SQLException {

        validarRuc(ruc);

        String sql = "UPDATE PROVEEDOR "
                + "SET Activo = ? WHERE RUC = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setString(2, ruc);

            return ps.executeUpdate() == 1;
        }
    }

    private void insertarTelefonos(Connection con, String ruc, List<String> telefonos) throws SQLException {

        String sql = "INSERT INTO TELEFONO_PROVEEDOR "
                + "(RUC_Proveedor, Telefono) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (String telefono : telefonos) {

                ps.setString(1, ruc);
                ps.setString(2, telefono);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "No se pudo insertar el teléfono.");
                }
            }
        }
    }

    private void eliminarTelefonos(Connection con, String ruc)
            throws SQLException {

        String sql = "DELETE FROM TELEFONO_PROVEEDOR "
                + "WHERE RUC_Proveedor = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ruc);

            ps.executeUpdate();
        }
    }

    private List<Proveedor> ejecutarListado(String sql, String ruc)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (ruc != null) {
                ps.setString(1, ruc);
            }

            try (ResultSet rs = ps.executeQuery()) {

                return construirProveedores(rs);
            }
        }
    }

    private List<Proveedor> construirProveedores(ResultSet rs)
            throws SQLException {

        List<Proveedor> proveedores = new ArrayList<>();

        boolean hayFila = rs.next();

        while (hayFila) {

            String ruc = rs.getString("RUC");
            String razonSocial = rs.getString("Razon_Social");
            String correo = rs.getString("Correo");
            String direccion = rs.getString("Direccion");
            boolean activo = rs.getBoolean("Activo");

            List<String> telefonos = new ArrayList<>();

            do {

                String telefono = rs.getString("Telefono");

                if (telefono != null) {
                    telefonos.add(telefono);
                }

                hayFila = rs.next();

            } while (hayFila && ruc.equals(rs.getString("RUC")));

            if (telefonos.isEmpty()) {
                throw new SQLException(
                        "El proveedor " + ruc + " no tiene teléfonos.");}

            Proveedor proveedor = new Proveedor(
                    ruc,
                    razonSocial,
                    correo,
                    direccion,
                    telefonos,
                    activo
            );

            proveedores.add(proveedor);
        }

        return proveedores;
    }

    private void validarRuc(String ruc) {

        if (ruc == null || ruc.isBlank() || ruc.length() != 13) {
            throw new IllegalArgumentException(
                    "El RUC debe tener 13 caracteres.");}
    }

    private Proveedor validarDatos(String ruc, String razonSocial, String correo, String direccion, List<String> telefonos) {

        validarRuc(ruc);

        Proveedor proveedor = new Proveedor(
                ruc, razonSocial, correo, direccion, telefonos, true
        );

        if (proveedor.getRazonSocial().length() > 150) {
            throw new IllegalArgumentException(
                    "La razón social no puede superar los 150 caracteres.");}
        if (proveedor.getCorreo().length() > 150) {
            throw new IllegalArgumentException(
                    "El correo no puede superar los 150 caracteres.");}
        if (proveedor.getDireccion().length() > 250) {
            throw new IllegalArgumentException(
                    "La dirección no puede superar los 250 caracteres.");}
        for (String telefono : proveedor.getTelefonos()) {
            if (telefono.length() > 15) {
                throw new IllegalArgumentException(
                        "El teléfono no puede superar los 15 caracteres.");}
        }

        return proveedor;
    }
}