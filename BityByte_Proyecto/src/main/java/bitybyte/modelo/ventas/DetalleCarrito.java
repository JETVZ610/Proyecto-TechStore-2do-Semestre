package bitybyte.modelo.ventas;

import bitybyte.modelo.inventario.Producto;

public class DetalleCarrito {
    private final int nroLinea;
    private final Producto producto;
    private int cantidad;

    public DetalleCarrito(int nroLinea, Producto producto, int cantidad) {
        if (nroLinea <= 0) {
            throw new IllegalArgumentException("El número de línea debe ser mayor que cero.");}
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");}
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");}

        this.nroLinea = nroLinea;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public int getNroLinea() {return nroLinea;}
    public Producto getProducto() {return producto;}
    public int getCantidad() {return cantidad;}

    public boolean modificarCantidad(int cantidad) {
        if (cantidad <= 0) {
            return false;}

        this.cantidad = cantidad;
        return true;
    }

    public double calcularSubtotal() {
        Double precioVenta = producto.getPrecioVenta();

        if (precioVenta == null) {
            throw new IllegalStateException(
                    "No se puede calcular el subtotal porque el producto no tiene precio de venta."
            );
        }

        return cantidad * precioVenta;
    }
}