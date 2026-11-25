package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.enums.EstadoProducto;
import bitybyte.modelo.inventario.Categoria;
import bitybyte.modelo.inventario.Marca;
import bitybyte.modelo.inventario.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Locale;

public class ProductoDAO {

    private static final String SELECT_BASE = """
            SELECT p.ID_Producto, p.Nombre_Producto,
                   p.Descripcion_Tecnica, p.Imagen_Producto,
                   p.Precio_Venta, p.Stock,
                   p.Stock_Minimo, p.Estado_Producto,
                   c.ID_Categoria, c.Nombre_Categoria,
                   c.Activo AS Categoria_Activa,
                   m.ID_Marca, m.Nombre_Marca,
                   m.Activo AS Marca_Activa
            FROM PRODUCTO p
            INNER JOIN CATEGORIA c
                ON p.ID_Categoria = c.ID_Categoria
            INNER JOIN MARCA m
                ON p.ID_Marca = m.ID_Marca
            """;

    private static final String ORDEN =
            " ORDER BY p.Nombre_Producto, p.ID_Producto";

    public Producto buscarPorId(int idProducto)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            return buscarPorId(con, idProducto);
        }
    }

    private Producto buscarPorId(Connection con, int idProducto) throws SQLException {

        String sql = SELECT_BASE + " WHERE p.ID_Producto = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() ? construirProducto(rs) : null;
            }
        }
    }

    public List<Producto> listar() throws SQLException {
        return ejecutarListado(SELECT_BASE + ORDEN);
    }

    public List<Producto> listarActivos() throws SQLException {
        return ejecutarListado(SELECT_BASE
                + " WHERE p.Estado_Producto = 'Activo'"
                + ORDEN);
    }

    public List<Producto> listarPendientes() throws SQLException {
        return ejecutarListado(SELECT_BASE
                + " WHERE p.Estado_Producto = 'Pendiente'"
                + ORDEN);
    }

    public List<Producto> listarDescontinuados() throws SQLException {
        return ejecutarListado(SELECT_BASE
                + " WHERE p.Estado_Producto = 'Descontinuado'"
                + ORDEN);
    }

    public List<Producto> listarStockBajo() throws SQLException {

        return ejecutarListado(SELECT_BASE
                + " WHERE p.Estado_Producto = 'Activo'"
                + " AND p.Stock <= p.Stock_Minimo"
                + ORDEN);
    }

    public Producto insertar(String nombre,
                             String descripcionTecnica,
                             String imagenProducto,
                             int idCategoria,
                             int idMarca,
                             Map<String, Double> suministros)
            throws SQLException {

        validarDatos(nombre, descripcionTecnica, imagenProducto,
                idCategoria, idMarca);

        Map<String, BigDecimal> precios =
                validarSuministros(suministros);

        String sql = """
                INSERT INTO PRODUCTO (
                    Nombre_Producto, Descripcion_Tecnica,
                    Imagen_Producto, Precio_Venta, Stock,
                    Stock_Minimo, Estado_Producto,
                    ID_Categoria, ID_Marca
                )
                SELECT ?, ?, ?, NULL, 0, NULL,
                       'Pendiente', c.ID_Categoria, m.ID_Marca
                FROM CATEGORIA c WITH (UPDLOCK, HOLDLOCK)
                CROSS JOIN MARCA m WITH (UPDLOCK, HOLDLOCK)
                WHERE c.ID_Categoria = ? AND c.Activo = 1
                  AND m.ID_Marca = ? AND m.Activo = 1
                """;

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {

                int idProducto;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setNString(1, nombre);
                    ps.setNString(2, descripcionTecnica);
                    ps.setNString(3, imagenProducto);
                    ps.setInt(4, idCategoria);
                    ps.setInt(5, idMarca);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "La categoría o marca no existe "
                                        + "o está inactiva.");}

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo recuperar el ID "
                                            + "del producto.");}
                        idProducto = rs.getInt(1);
                    }
                }

                insertarSuministros(con, idProducto, precios);

                Producto producto = buscarPorId(con, idProducto);

                if (producto == null) {
                    throw new SQLException(
                            "No se pudo recuperar el producto registrado.");}
                con.commit();

                return producto;

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

    private void insertarSuministros(
            Connection con,
            int idProducto,
            Map<String, BigDecimal> precios)
            throws SQLException {

        String sql = """
                INSERT INTO SUMINISTRA (
                    RUC_Proveedor, ID_Producto,
                    Precio_Compra_Ofrecido
                )
                SELECT RUC, ?, ?
                FROM PROVEEDOR WITH (UPDLOCK, HOLDLOCK)
                WHERE RUC = ? AND Activo = 1
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (Map.Entry<String, BigDecimal> entrada
                    : precios.entrySet()) {

                ps.setInt(1, idProducto);
                ps.setBigDecimal(2, entrada.getValue());
                ps.setString(3, entrada.getKey());

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "Proveedor inexistente o inactivo: "
                                    + entrada.getKey());
                }
            }
        }
    }

    public boolean actualizarDatos(int idProducto, String nombre, String descripcionTecnica, String imagenProducto, int idCategoria, int idMarca)
            throws SQLException {
        validarDatos(nombre, descripcionTecnica, imagenProducto,
                idCategoria, idMarca);

        String sql = """
                UPDATE p
                SET Nombre_Producto = ?,
                    Descripcion_Tecnica = ?,
                    Imagen_Producto = ?,
                    ID_Categoria = c.ID_Categoria,
                    ID_Marca = m.ID_Marca
                FROM PRODUCTO p
                INNER JOIN CATEGORIA c WITH (UPDLOCK, HOLDLOCK)
                    ON c.ID_Categoria = ?
                INNER JOIN MARCA m WITH (UPDLOCK, HOLDLOCK)
                    ON m.ID_Marca = ?
                WHERE p.ID_Producto = ?
                  AND (c.Activo = 1
                       OR p.ID_Categoria = c.ID_Categoria)
                  AND (m.Activo = 1
                       OR p.ID_Marca = m.ID_Marca)
                """;

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setNString(1, nombre);
            ps.setNString(2, descripcionTecnica);
            ps.setNString(3, imagenProducto);
            ps.setInt(4, idCategoria);
            ps.setInt(5, idMarca);
            ps.setInt(6, idProducto);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean establecerPrecioVenta(int idProducto,
                                         double precio)
            throws SQLException {

        BigDecimal valor = validarPrecio(precio);

        String sql = "UPDATE PRODUCTO "
                + "SET Precio_Venta = ? "
                + "WHERE ID_Producto = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, valor);
            ps.setInt(2, idProducto);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean establecerStockMinimo(int idProducto,
                                         int stockMinimo)
            throws SQLException {

        if (stockMinimo < 0) {
            throw new IllegalArgumentException(
                    "El stock mínimo no puede ser negativo.");}

        String sql = "UPDATE PRODUCTO "
                + "SET Stock_Minimo = ? "
                + "WHERE ID_Producto = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, stockMinimo);
            ps.setInt(2, idProducto);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean descontinuar(int idProducto)
            throws SQLException {

        String sql = "UPDATE PRODUCTO "
                + "SET Estado_Producto = 'Descontinuado' "
                + "WHERE ID_Producto = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            return ps.executeUpdate() == 1;
        }
    }

    private List<Producto> ejecutarListado(String sql)
            throws SQLException {

        List<Producto> productos = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                productos.add(construirProducto(rs));
            }
        }

        return productos;
    }

    Producto construirProducto(ResultSet rs) throws SQLException {

        Categoria categoria = new Categoria(
                rs.getInt("ID_Categoria"),
                rs.getString("Nombre_Categoria"),
                rs.getBoolean("Categoria_Activa")
        );

        Marca marca = new Marca(
                rs.getInt("ID_Marca"),
                rs.getString("Nombre_Marca"),
                rs.getBoolean("Marca_Activa")
        );

        BigDecimal precioSQL = rs.getBigDecimal("Precio_Venta");

        Double precioVenta = precioSQL == null
                ? null : precioSQL.doubleValue();

        int minimo = rs.getInt("Stock_Minimo");

        Integer stockMinimo = rs.wasNull() ? null : minimo;

        EstadoProducto estado;

        try {

            estado = EstadoProducto.valueOf(
                    rs.getString("Estado_Producto")
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException e) {

            throw new SQLException(
                    "Estado de producto desconocido.", e);}

        return new Producto(
                rs.getInt("ID_Producto"),
                rs.getString("Nombre_Producto"),
                rs.getString("Descripcion_Tecnica"),
                rs.getString("Imagen_Producto"),
                precioVenta,
                rs.getInt("Stock"),
                stockMinimo,
                estado,
                categoria,
                marca
        );
    }

    private void validarDatos(String nombre, String descripcionTecnica, String imagenProducto, int idCategoria, int idMarca) {

        validarTexto(nombre, 150, "El nombre");

        validarTexto(descripcionTecnica, 500,
                "La descripción técnica");

        validarTexto(imagenProducto, 500,
                "La referencia de imagen");

        if (idCategoria <= 0 || idMarca <= 0) {

            throw new IllegalArgumentException(
                    "Los identificadores de categoría "
                            + "y marca deben ser positivos.");}
    }

    private void validarTexto(String texto, int maximo, String campo) {

        if (texto == null || texto.isBlank()
                || texto.length() > maximo) {

            throw new IllegalArgumentException(
                    campo + " debe contener entre 1 y "
                            + maximo + " caracteres.");}
    }

    private Map<String, BigDecimal> validarSuministros(
            Map<String, Double> suministros) {

        if (suministros == null || suministros.isEmpty()) {

            throw new IllegalArgumentException(
                    "El producto debe tener al menos un proveedor.");}

        Map<String, BigDecimal> precios = new TreeMap<>();

        for (Map.Entry<String, Double> entrada
                : suministros.entrySet()) {

            String ruc = entrada.getKey();

            if (ruc == null || ruc.isBlank()
                    || ruc.length() != 13) {
                throw new IllegalArgumentException(
                        "El RUC debe tener 13 caracteres.");}

            precios.put(ruc, validarPrecio(entrada.getValue()));
        }

        return precios;
    }

    private BigDecimal validarPrecio(Double precio) {
        if (precio == null || !Double.isFinite(precio)
                || precio <= 0 || precio > 99999999.99) {
            throw new IllegalArgumentException(
                    "El precio debe ser positivo y no "
                            + "superar 99999999.99.");}
        try {
            return BigDecimal.valueOf(precio)
                    .setScale(2, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "El precio debe tener como máximo "
                            + "dos decimales.", e);}
    }
}