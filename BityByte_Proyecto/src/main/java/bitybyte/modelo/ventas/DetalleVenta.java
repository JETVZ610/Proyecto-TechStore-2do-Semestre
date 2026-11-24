package bitybyte.modelo.ventas;

import bitybyte.modelo.inventario.Producto;

public class DetalleVenta {
    private final int nroLinea;
    private final Producto producto;
    private int cantidad;
    private final double precioUnitarioVenta;

    public DetalleVenta(int nroLinea, Producto producto, int cantidad, double precioUnitarioVenta){
        if(nroLinea <= 0){
            throw new IllegalArgumentException("El número de línea debe ser mayor que cero.");}
        if(producto == null){
            throw new IllegalArgumentException("El producto no puede ser nulo.");}
        if(cantidad <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");}
        if(precioUnitarioVenta <= 0){
            throw new IllegalArgumentException("El precio unitario de venta debe ser mayor que cero.");}

        this.nroLinea = nroLinea;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitarioVenta = precioUnitarioVenta;
    }

    public int getNroLinea(){return nroLinea;}
    public Producto getProducto(){return producto;}
    public int getCantidad(){return cantidad;}
    public double getPrecioUnitarioVenta(){return precioUnitarioVenta;}

    boolean modificarCantidad(int cantidad){
        if(cantidad <= 0){
            return false;
        }

        this.cantidad = cantidad;
        return true;
    }

    public double calcularSubtotal(){return precioUnitarioVenta * cantidad;}
}