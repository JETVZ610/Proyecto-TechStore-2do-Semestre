package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.enums.EstadoProducto;
import bitybyte.modelo.compras.Proveedor;
import bitybyte.modelo.compras.Suministro;
import bitybyte.modelo.inventario.Categoria;
import bitybyte.modelo.inventario.Marca;
import bitybyte.modelo.inventario.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuministroDAO {

    private static final String SELECT_BASE = """
            SELECT s.RUC_Proveedor, s.Precio_Compra_Ofrecido,
                   pr.Razon_Social, pr.Correo, pr.Direccion,
                   pr.Activo AS Proveedor_Activo, t.Telefono,
                   p.ID_Producto, p.Nombre_Producto,
                   p.Descripcion_Tecnica, p.Imagen_Producto,
                   p.Precio_Venta, p.Stock, p.Stock_Minimo,
                   p.Estado_Producto,
                   c.ID_Categoria, c.Nombre_Categoria,
                   c.Activo AS Categoria_Activa,
                   m.ID_Marca, m.Nombre_Marca,
                   m.Activo AS Marca_Activa
            FROM SUMINISTRA s
            INNER JOIN PROVEEDOR pr 
                ON s.RUC_Proveedor = pr.RUC
            INNER JOIN PRODUCTO p 
                ON s.ID_Producto = p.ID_Producto
            INNER JOIN CATEGORIA c 
                ON p.ID_Categoria = c.ID_Categoria
            INNER JOIN MARCA m 
                ON p.ID_Marca = m.ID_Marca
            LEFT JOIN TELEFONO_PROVEEDOR t 
                ON pr.RUC = t.RUC_Proveedor
            """;

    private static final String ORDEN =
            " ORDER BY s.RUC_Proveedor, s.ID_Producto, t.Telefono";

    private static final String DISPONIBLE =
            " AND pr.Activo = 1"
                    + " AND p.Estado_Producto <> 'Descontinuado'";

    public Suministro buscar(String ruc, int idProducto)
            throws SQLException {

        validarRuc(ruc);
        validarIdProducto(idProducto);

        List<Suministro> resultados = ejecutarListado(
                SELECT_BASE
                        + " WHERE s.RUC_Proveedor = ?"
                        + " AND s.ID_Producto = ?"
                        + ORDEN,
                ruc, idProducto
        );

        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public List<Suministro> listar() throws SQLException {

        return ejecutarListado(
                SELECT_BASE + ORDEN, null, null
        );
    }

    public List<Suministro> listarPorProveedor(String ruc)
            throws SQLException {

        validarRuc(ruc);

        return ejecutarListado(
                SELECT_BASE
                        + " WHERE s.RUC_Proveedor = ?"
                        + ORDEN,
                ruc, null
        );
    }

    public List<Suministro> listarPorProducto(int idProducto)
            throws SQLException {

        validarIdProducto(idProducto);

        return ejecutarListado(
                SELECT_BASE
                        + " WHERE s.ID_Producto = ?"
                        + ORDEN,
                null, idProducto
        );
    }

    public List<Suministro> listarDisponiblesPorProducto(
            int idProducto) throws SQLException {

        validarIdProducto(idProducto);

        return ejecutarListado(
                SELECT_BASE
                        + " WHERE s.ID_Producto = ?"
                        + DISPONIBLE
                        + ORDEN,
                null, idProducto
        );
    }

    public List<Suministro> listarDisponiblesPorProveedor(
            String ruc) throws SQLException {

        validarRuc(ruc);

        return ejecutarListado(
                SELECT_BASE
                        + " WHERE s.RUC_Proveedor = ?"
                        + DISPONIBLE
                        + ORDEN,
                ruc, null
        );
    }

    public boolean insertar(String ruc, int idProducto,
                            double precio) throws SQLException {

        validarRuc(ruc);
        validarIdProducto(idProducto);

        BigDecimal valor = validarPrecio(precio);

        String sql = """
                INSERT INTO SUMINISTRA (
                    RUC_Proveedor, ID_Producto,
                    Precio_Compra_Ofrecido
                )
                SELECT pr.RUC, p.ID_Producto, ?
                FROM PRODUCTO p WITH (UPDLOCK, HOLDLOCK)
                CROSS JOIN PROVEEDOR pr WITH (UPDLOCK, HOLDLOCK)
                WHERE pr.RUC = ? AND pr.Activo = 1
                  AND p.ID_Producto = ?
                  AND p.Estado_Producto <> 'Descontinuado'
                """;

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, valor);
            ps.setString(2, ruc);
            ps.setInt(3, idProducto);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean actualizarPrecioCompraOfrecido(
            String ruc, int idProducto, double precio)
            throws SQLException {

        validarRuc(ruc);
        validarIdProducto(idProducto);

        BigDecimal valor = validarPrecio(precio);

        String sql = """
                UPDATE s
                SET Precio_Compra_Ofrecido = ?
                FROM SUMINISTRA s
                INNER JOIN PRODUCTO p WITH (UPDLOCK, HOLDLOCK)
                    ON s.ID_Producto = p.ID_Producto
                INNER JOIN PROVEEDOR pr WITH (UPDLOCK, HOLDLOCK)
                    ON s.RUC_Proveedor = pr.RUC
                WHERE s.RUC_Proveedor = ? 
                  AND s.ID_Producto = ?
                  AND pr.Activo = 1
                  AND p.Estado_Producto <> 'Descontinuado'
                """;

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, valor);
            ps.setString(2, ruc);
            ps.setInt(3, idProducto);

            return ps.executeUpdate() == 1;
        }
    }

    private List<Suministro> ejecutarListado(
            String sql, String ruc, Integer idProducto)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int indice = 1;

            if (ruc != null) {
                ps.setString(indice++, ruc);
            }

            if (idProducto != null) {
                ps.setInt(indice, idProducto);
            }

            try (ResultSet rs = ps.executeQuery()) {

                return construirSuministros(rs);
            }
        }
    }

    private List<Suministro> construirSuministros(ResultSet rs)
            throws SQLException {

        List<Suministro> suministros = new ArrayList<>();

        boolean hayFila = rs.next();

        while (hayFila) {

            String ruc = rs.getString("RUC_Proveedor");
            int idProducto = rs.getInt("ID_Producto");

            String razonSocial = rs.getString("Razon_Social");
            String correo = rs.getString("Correo");
            String direccion = rs.getString("Direccion");

            boolean activo = rs.getBoolean("Proveedor_Activo");

            double precio = rs.getBigDecimal(
                    "Precio_Compra_Ofrecido").doubleValue();

            Producto producto = construirProducto(rs);

            List<String> telefonos = new ArrayList<>();

            do {

                String telefono = rs.getString("Telefono");

                if (telefono != null) {
                    telefonos.add(telefono);
                }

                hayFila = rs.next();

            } while (hayFila
                    && ruc.equals(rs.getString("RUC_Proveedor"))
                    && idProducto == rs.getInt("ID_Producto"));

            if (telefonos.isEmpty()) {
                throw new SQLException(
                        "El proveedor " + ruc + " no tiene teléfonos.");
            }

            Proveedor proveedor = new Proveedor(
                    ruc,
                    razonSocial,
                    correo,
                    direccion,
                    telefonos,
                    activo
            );

            Suministro suministro = new Suministro(
                    proveedor,
                    producto,
                    precio
            );

            suministros.add(suministro);
        }

        return suministros;
    }

    private Producto construirProducto(ResultSet rs)
            throws SQLException {

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

    private void validarRuc(String ruc) {
        if (ruc == null || ruc.isBlank() || ruc.length() != 13) {

            throw new IllegalArgumentException(
                    "El RUC debe tener 13 caracteres.");}
    }

    private void validarIdProducto(int idProducto) {
        if (idProducto <= 0) {

            throw new IllegalArgumentException(
                    "El ID del producto debe ser positivo.");}
    }

    private BigDecimal validarPrecio(double precio) {

        if (!Double.isFinite(precio)
                || precio <= 0 || precio > 99999999.99) {

            throw new IllegalArgumentException(
                    "El precio debe ser positivo y no " + "superar 99999999.99.");}

        try {

            return BigDecimal.valueOf(precio)
                    .setScale(2, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "El precio debe tener como máximo " + "dos decimales.", e);
        }
    }
}