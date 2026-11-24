package bitybyte.modelo.compras;

import bitybyte.modelo.inventario.Producto;

public class DetalleCompra {
    private final int nroLinea;
    private final Producto producto;
    private int cantidad;
    private final double precioUnitarioCompra;

    public DetalleCompra(int nroLinea, Producto producto, int cantidad, double precioUnitarioCompra){
        if(nroLinea <= 0){
            throw new IllegalArgumentException("El número de línea debe ser mayor que cero.");}
        if(producto == null){
            throw new IllegalArgumentException("El producto no puede ser nulo.");}
        if(cantidad <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");}
        if(precioUnitarioCompra <= 0){
            throw new IllegalArgumentException("El precio unitario de compra debe ser mayor que cero.");}

        this.nroLinea = nroLinea;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitarioCompra = precioUnitarioCompra;
    }

    public int getNroLinea(){return nroLinea;}
    public Producto getProducto(){return producto;}
    public int getCantidad(){return cantidad;}
    public double getPrecioUnitarioCompra(){return precioUnitarioCompra;}

    boolean modificarCantidad(int cantidad){
        if(cantidad <= 0){
            return false;}

        this.cantidad = cantidad;
        return true;
    }

    public double calcularSubtotal(){return precioUnitarioCompra * cantidad;}
}