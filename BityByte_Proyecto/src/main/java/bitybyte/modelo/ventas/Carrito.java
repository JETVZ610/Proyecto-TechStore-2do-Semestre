package bitybyte.modelo.ventas;

import bitybyte.modelo.inventario.Producto;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private final int idCarrito;
    private final List<DetalleCarrito> detalles;

    public Carrito(int idCarrito) {
        this.idCarrito = idCarrito;
        this.detalles = new ArrayList<>();
    }

    public Carrito(int idCarrito, List<DetalleCarrito> detalles) {
        this.idCarrito = idCarrito;
        this.detalles = new ArrayList<>(detalles);
    }

    public int getIdCarrito() {return idCarrito;}
    public List<DetalleCarrito> getDetalles() {return List.copyOf(detalles);}

    public boolean agregarProducto(Producto producto, int cantidad) {
        if (producto == null || cantidad <= 0) {
            return false;
        }

        DetalleCarrito detalleExistente = buscarDetalle(producto);

        if (detalleExistente != null) {
            int nuevaCantidad = detalleExistente.getCantidad() + cantidad;
            return detalleExistente.modificarCantidad(nuevaCantidad);
        }

        DetalleCarrito nuevoDetalle = new DetalleCarrito(
                siguienteNroLinea(),
                producto,
                cantidad
        );

        detalles.add(nuevoDetalle);
        return true;
    }

    public boolean modificarCantidad(Producto producto, int cantidad) {
        if (producto == null || cantidad <= 0) {
            return false;
        }

        DetalleCarrito detalle = buscarDetalle(producto);

        if (detalle == null) {
            return false;
        }

        return detalle.modificarCantidad(cantidad);
    }

    public boolean retirarProducto(Producto producto) {
        if (producto == null) {
            return false;
        }

        DetalleCarrito detalle = buscarDetalle(producto);

        if (detalle == null) {
            return false;
        }

        return detalles.remove(detalle);
    }

    public double calcularSubtotal() {
        double subtotal = 0;

        for (DetalleCarrito detalle : detalles) {
            subtotal += detalle.calcularSubtotal();
        }

        return subtotal;
    }

    public boolean estaVacio() {return detalles.isEmpty();}
    public void vaciar() {detalles.clear();}

    private DetalleCarrito buscarDetalle(Producto producto) {
        for (DetalleCarrito detalle : detalles) {
            if (detalle.getProducto().getIdProducto() == producto.getIdProducto()) {
                return detalle;
            }
        }

        return null;
    }

    private int siguienteNroLinea() {
        int mayorNroLinea = 0;

        for (DetalleCarrito detalle : detalles) {
            if (detalle.getNroLinea() > mayorNroLinea) {
                mayorNroLinea = detalle.getNroLinea();
            }
        }

        return mayorNroLinea + 1;
    }

    public void cargarDesdePedido(Pedido pedido) {
        if (pedido == null) {
            return;
        }

        for (DetalleVenta detalleVenta : pedido.getDetalles()) {
            Producto producto = detalleVenta.getProducto();

            if (producto != null && producto.estaActivo()) {
                agregarProducto(producto, detalleVenta.getCantidad());
            }
        }
    }
}