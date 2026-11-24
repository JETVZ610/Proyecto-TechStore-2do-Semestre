package bitybyte.modelo.ventas;

import bitybyte.modelo.inventario.Producto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Venta {
    private final int idVenta;
    private final LocalDateTime fechaVenta;
    private final List<DetalleVenta> detalles;
    private Pago pago;
    private Factura factura;

    public Venta(int idVenta, LocalDateTime fechaVenta, List<DetalleVenta> detalles){
        if(fechaVenta == null){
            throw new IllegalArgumentException("La fecha de venta no puede ser nula.");}

        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.pago = null;
        this.factura = null;
    }

    public int getIdVenta(){return idVenta;}
    public LocalDateTime getFechaVenta(){return fechaVenta;}
    public List<DetalleVenta> getDetalles(){return List.copyOf(detalles);}
    public Pago getPago(){return pago;}
    public Factura getFactura(){return factura;}

    public boolean agregarDetalle(Producto producto, int cantidad){
        if(estaConfirmada()){
            return false;}
        if(producto == null || cantidad <= 0){
            return false;}

        Double precioActual = producto.getPrecioVenta();

        if(precioActual == null){
            return false;}

        DetalleVenta detalleExistente = buscarDetalle(producto);

        if(detalleExistente != null){
            int nuevaCantidad = detalleExistente.getCantidad() + cantidad;

            if(!producto.puedeVenderse(nuevaCantidad)){
                return false;}

            int indice = detalles.indexOf(detalleExistente);

            detalles.set(indice, new DetalleVenta(
                    detalleExistente.getNroLinea(),
                    producto,
                    nuevaCantidad,
                    precioActual
            ));

            return true;}

        if(!producto.puedeVenderse(cantidad)){
            return false;}

        DetalleVenta nuevoDetalle = new DetalleVenta(
                siguienteNroLinea(),
                producto,
                cantidad,
                precioActual
        );

        detalles.add(nuevoDetalle);
        return true;
    }

    public boolean modificarCantidad(Producto producto, int cantidad){
        if(estaConfirmada()){
            return false;}

        if(producto == null || cantidad <= 0){
            return false;}

        DetalleVenta detalle = buscarDetalle(producto);

        if(detalle == null){
            return false;}

        Double precioActual = producto.getPrecioVenta();

        if(precioActual == null || !producto.puedeVenderse(cantidad)){
            return false;}

        int indice = detalles.indexOf(detalle);

        detalles.set(indice, new DetalleVenta(
                detalle.getNroLinea(),
                producto,
                cantidad,
                precioActual
        ));

        return true;
    }

    public boolean retirarProducto(Producto producto){
        if(estaConfirmada()){
            return false;}

        if(producto == null){
            return false;}

        DetalleVenta detalle = buscarDetalle(producto);

        if(detalle == null){
            return false;}

        return detalles.remove(detalle);
    }

    public double calcularSubtotal(){
        double subtotal = 0;

        for(DetalleVenta detalle : detalles){
            subtotal += detalle.calcularSubtotal();
        }

        return subtotal;
    }

    public boolean estaConfirmada(){return pago != null && factura != null;}

    protected void restaurarConfirmacionHistorica(Pago pago, Factura factura){
        if(pago == null || factura == null){
            throw new IllegalArgumentException(
                    "Una venta histórica confirmada debe tener pago y factura."
            );}
        if(estaConfirmada()){
            throw new IllegalStateException("La venta ya se encuentra confirmada.");}
        if(detalles.isEmpty()){
            throw new IllegalArgumentException(
                    "Una venta histórica confirmada debe tener al menos un detalle."
            );}
        if(factura.getVenta() != this){
            throw new IllegalArgumentException(
                    "La factura debe pertenecer a esta venta."
            );}
        if(Math.abs(pago.getMonto() - factura.calcularTotal()) > 0.01){
            throw new IllegalArgumentException(
                    "El monto histórico del pago no coincide con el total de la factura."
            );}

        this.pago = pago;
        this.factura = factura;
    }

    protected boolean preciosVigentes(){
        for(DetalleVenta detalle : detalles){
            Double precioActual = detalle.getProducto().getPrecioVenta();

            if(precioActual == null){
                return false;}

            if(Math.abs(detalle.getPrecioUnitarioVenta() - precioActual) > 0.001){
                return false;}
        }

        return true;
    }

    public abstract boolean confirmar(Pago pago, Factura factura);

    protected void establecerPago(Pago pago){this.pago = pago;}

    protected void establecerFactura(Factura factura){this.factura = factura;}

    private DetalleVenta buscarDetalle(Producto producto){
        for(DetalleVenta detalle : detalles){
            if(detalle.getProducto().getIdProducto() == producto.getIdProducto()){
                return detalle;
            }
        }

        return null;
    }

    private int siguienteNroLinea(){
        int mayorNroLinea = 0;

        for(DetalleVenta detalle : detalles){
            if(detalle.getNroLinea() > mayorNroLinea){
                mayorNroLinea = detalle.getNroLinea();
            }
        }

        return mayorNroLinea + 1;
    }
}