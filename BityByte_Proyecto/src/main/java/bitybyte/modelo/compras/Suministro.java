package bitybyte.modelo.compras;

import bitybyte.modelo.inventario.Producto;

public class Suministro {
    private final Proveedor proveedor;
    private final Producto producto;
    private double precioCompraOfrecido;

    public Suministro(Proveedor proveedor, Producto producto, double precioCompraOfrecido){
        if(proveedor == null){
            throw new IllegalArgumentException("El proveedor no puede ser nulo.");}
        if(producto == null){
            throw new IllegalArgumentException("El producto no puede ser nulo.");}
        if(precioCompraOfrecido <= 0){
            throw new IllegalArgumentException("El precio de compra ofrecido debe ser mayor que cero.");}

        this.proveedor = proveedor;
        this.producto = producto;
        this.precioCompraOfrecido = precioCompraOfrecido;
    }

    public Proveedor getProveedor(){return proveedor;}
    public Producto getProducto(){return producto;}
    public double getPrecioCompraOfrecido(){return precioCompraOfrecido;}

    public boolean actualizarPrecioCompraOfrecido(double precio){
        if(precio <= 0){
            return false;
        }

        this.precioCompraOfrecido = precio;
        return true;
    }
}