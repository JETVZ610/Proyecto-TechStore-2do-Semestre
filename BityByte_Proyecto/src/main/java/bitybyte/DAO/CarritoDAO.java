package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.ventas.Carrito;
import bitybyte.modelo.ventas.DetalleCarrito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarritoDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();

    private static final String SELECT_BASE = """
            SELECT ca.ID_Carrito, d.Nro_Linea, d.Cantidad,
                   p.ID_Producto, p.Nombre_Producto,
                   p.Descripcion_Tecnica, p.Imagen_Producto,
                   p.Precio_Venta, p.Stock, p.Stock_Minimo,
                   p.Estado_Producto,
                   c.ID_Categoria, c.Nombre_Categoria,
                   c.Activo AS Categoria_Activa,
                   m.ID_Marca, m.Nombre_Marca,
                   m.Activo AS Marca_Activa
            FROM CARRITO ca
            LEFT JOIN DETALLE_CARRITO d
                ON ca.ID_Carrito = d.ID_Carrito
            LEFT JOIN PRODUCTO p
                ON d.ID_Producto = p.ID_Producto
            LEFT JOIN CATEGORIA c
                ON p.ID_Categoria = c.ID_Categoria
            LEFT JOIN MARCA m
                ON p.ID_Marca = m.ID_Marca
            """;

    public Carrito buscarPorId(int idCarrito) throws SQLException {
        try (Connection con = Conexion.obtenerConexion()) {
            return buscarPorId(con, idCarrito);
        }
    }

    Carrito buscarPorId(Connection con, int idCarrito) throws SQLException {
        validarId(idCarrito);
        return buscarCarrito(
                con,
                " WHERE ca.ID_Carrito = ?",
                idCarrito
        );
    }

    public Carrito buscarPorCliente(int idPersona) throws SQLException {
        try (Connection con = Conexion.obtenerConexion()) {
            return buscarPorCliente(con, idPersona);
        }
    }

    Carrito buscarPorCliente(Connection con, int idPersona) throws SQLException {
        validarId(idPersona);
        return buscarCarrito(
                con,
                " WHERE ca.ID_Persona_ClienteOnline = ?",
                idPersona
        );
    }

    private Carrito buscarCarrito(Connection con, String filtro, int id)
            throws SQLException {
        String sql = SELECT_BASE + filtro
                + " ORDER BY d.Nro_Linea";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int idCarrito = rs.getInt("ID_Carrito");
                List<DetalleCarrito> detalles = new ArrayList<>();

                do {
                    int nroLinea = rs.getInt("Nro_Linea");
                    if (!rs.wasNull()) {
                        detalles.add(new DetalleCarrito(
                                nroLinea,
                                productoDAO.construirProducto(rs),
                                rs.getInt("Cantidad")
                        ));
                    }
                } while (rs.next());

                return new Carrito(idCarrito, detalles);
            }
        }
    }

    Carrito insertar(Connection con, int idPersona) throws SQLException {
        validarId(idPersona);
        if (con.getAutoCommit()) {
            throw new SQLException(
                    "Se requiere una transacción compartida.");}

        String sql = "INSERT INTO CARRITO "
                + "(ID_Persona_ClienteOnline) VALUES (?)";

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idPersona);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se pudo crear el carrito.");}

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo recuperar el ID del carrito.");}

                return new Carrito(rs.getInt(1));
            }
        }
    }

    public boolean agregarProducto(int idCarrito, int idProducto, int cantidad)
            throws SQLException {
        validarId(idProducto);
        validarCantidad(cantidad);

        return ejecutarCambio(idCarrito, con -> {
            int cantidadActual = obtenerCantidad(
                    con, idCarrito, idProducto
            );

            long total = (long) cantidadActual + cantidad;

            if (total > Integer.MAX_VALUE) {

                throw new IllegalArgumentException(
                        "La cantidad supera el límite permitido.");}

            int nuevaCantidad = (int) total;

            if (!productoDisponible(
                    con, idProducto, nuevaCantidad)) {

                return false;
            }

            if (cantidadActual > 0) {
                return actualizarCantidad(
                        con, idCarrito,
                        idProducto, nuevaCantidad
                );
            }

            int nroLinea = siguienteNroLinea(con, idCarrito);

            String sql = """
                    INSERT INTO DETALLE_CARRITO (
                        ID_Carrito, Nro_Linea,
                        ID_Producto, Cantidad
                    )
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, idCarrito);
                ps.setInt(2, nroLinea);
                ps.setInt(3, idProducto);
                ps.setInt(4, cantidad);

                return ps.executeUpdate() == 1;
            }
        });
    }

    public boolean modificarCantidad(int idCarrito, int idProducto, int cantidad) throws SQLException {
        validarId(idProducto);
        validarCantidad(cantidad);
        return ejecutarCambio(idCarrito, con -> {
            int cantidadActual = obtenerCantidad(
                    con, idCarrito, idProducto
            );
            if (cantidadActual == 0) {
                return false;
            }

            if (cantidad > cantidadActual
                    && !productoDisponible(
                    con, idProducto, cantidad)) {

                return false;
            }

            return actualizarCantidad(
                    con, idCarrito, idProducto, cantidad
            );
        });
    }

    public boolean retirarProducto(int idCarrito, int idProducto) throws SQLException {
        validarId(idProducto);
        return ejecutarCambio(idCarrito, con -> {

            String sql = "DELETE FROM DETALLE_CARRITO "
                    + "WHERE ID_Carrito = ? "
                    + "AND ID_Producto = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCarrito);
                ps.setInt(2, idProducto);

                return ps.executeUpdate() == 1;
            }
        });
    }

    public boolean vaciar(int idCarrito) throws SQLException {
        return ejecutarCambio(idCarrito, con -> {
            String sql = "DELETE FROM DETALLE_CARRITO "
                    + "WHERE ID_Carrito = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCarrito);
                ps.executeUpdate();

                return true;
            }
        });
    }

    private boolean actualizarCantidad(Connection con, int idCarrito, int idProducto, int cantidad) throws SQLException {

        String sql = "UPDATE DETALLE_CARRITO "
                + "SET Cantidad = ? "
                + "WHERE ID_Carrito = ? "
                + "AND ID_Producto = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cantidad);
            ps.setInt(2, idCarrito);
            ps.setInt(3, idProducto);

            return ps.executeUpdate() == 1;
        }
    }

    private int obtenerCantidad(Connection con, int idCarrito, int idProducto) throws SQLException {
        String sql = "SELECT Cantidad FROM DETALLE_CARRITO "
                + "WHERE ID_Carrito = ? "
                + "AND ID_Producto = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            ps.setInt(2, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getInt("Cantidad") : 0;
            }
        }
    }

    private int siguienteNroLinea(Connection con, int idCarrito) throws SQLException {
        String sql = """
                SELECT ISNULL(MAX(Nro_Linea), 0) AS MayorLinea
                FROM DETALLE_CARRITO
                WHERE ID_Carrito = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int mayorLinea = rs.getInt("MayorLinea");
                if (mayorLinea == Integer.MAX_VALUE) {
                    throw new SQLException(
                            "Se alcanzó el límite de líneas.");}

                return mayorLinea + 1;
            }
        }
    }

    private boolean productoDisponible(Connection con, int idProducto, int cantidad) throws SQLException {

        String sql = """
                SELECT ID_Producto
                FROM PRODUCTO
                WHERE ID_Producto = ?
                  AND Estado_Producto = 'Activo'
                  AND Precio_Venta IS NOT NULL
                  AND Stock >= ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, cantidad);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean bloquearCarrito(Connection con, int idCarrito) throws SQLException {

        String sql = """
                SELECT ca.ID_Carrito
                FROM CARRITO ca WITH (UPDLOCK, HOLDLOCK)
                INNER JOIN CLIENTE_ONLINE co
                    WITH (UPDLOCK, HOLDLOCK)
                    ON ca.ID_Persona_ClienteOnline = co.ID_Persona
                WHERE ca.ID_Carrito = ?
                  AND co.Activo = 1
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    @FunctionalInterface
    private interface OperacionCarrito {
        boolean ejecutar(Connection con) throws SQLException;
    }

    private boolean ejecutarCambio(int idCarrito, OperacionCarrito operacion) throws SQLException {
        validarId(idCarrito);
        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);

            try {
                if (!bloquearCarrito(con, idCarrito)) {
                    con.rollback();
                    return false;
                }
                boolean resultado = operacion.ejecutar(con);
                if (resultado) {
                    con.commit();
                } else {
                    con.rollback();
                }

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
                    "El identificador debe ser positivo.");}
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero.");}
    }
}