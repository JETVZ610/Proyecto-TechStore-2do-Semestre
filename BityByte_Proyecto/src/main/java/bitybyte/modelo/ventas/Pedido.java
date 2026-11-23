package bitybyte.modelo.ventas;

import bitybyte.enums.EstadoPago;
import bitybyte.enums.EstadoPedido;
import bitybyte.enums.OperacionEmpleado;
import bitybyte.modelo.personas.ClienteOnline;
import bitybyte.modelo.personas.Direccion;
import bitybyte.modelo.personas.Empleado;

import java.time.LocalDateTime;
import java.util.List;

public class Pedido extends Venta {
    private final ClienteOnline clienteOnline;
    private final Direccion direccionEnvio;
    private EstadoPedido estadoPedido;

    public Pedido(int idVenta, LocalDateTime fechaVenta, List<DetalleVenta> detalles,
                  ClienteOnline clienteOnline, Direccion direccionEnvio,
                  EstadoPedido estadoPedido){

        super(idVenta, fechaVenta, detalles);

        if(clienteOnline == null){
            throw new IllegalArgumentException("El cliente online no puede ser nulo.");}
        if(direccionEnvio == null){
            throw new IllegalArgumentException("La dirección de envío no puede ser nula.");}

        this.clienteOnline = clienteOnline;
        this.direccionEnvio = direccionEnvio;
        this.estadoPedido = estadoPedido;
    }

    public static Pedido reconstruirHistorico(
            int idVenta,
            LocalDateTime fechaVenta,
            List<DetalleVenta> detalles,
            ClienteOnline clienteOnline,
            Direccion direccionEnvio,
            EstadoPedido estadoPedido,
            Pago pago,
            int idFactura,
            LocalDateTime fechaEmisionFactura,
            TasaIVA tasaIVA){

        if(estadoPedido == null){
            throw new IllegalArgumentException(
                    "Un pedido histórico debe tener un estado."
            );}
        if(pago == null){
            throw new IllegalArgumentException(
                    "Un pedido histórico confirmado debe tener un pago."
            );}
        if(estadoPedido == EstadoPedido.CANCELADO
                && pago.getEstado() != EstadoPago.REEMBOLSADO){
            throw new IllegalArgumentException(
                    "Un pedido cancelado debe tener su pago reembolsado."
            );}
        if(estadoPedido != EstadoPedido.CANCELADO
                && pago.getEstado() != EstadoPago.PAGADO){
            throw new IllegalArgumentException(
                    "Un pedido no cancelado debe tener su pago en estado pagado."
            );}

        Pedido pedido = new Pedido(
                idVenta,
                fechaVenta,
                detalles,
                clienteOnline,
                direccionEnvio,
                estadoPedido
        );

        Factura factura = new Factura(
                idFactura,
                fechaEmisionFactura,
                pedido,
                tasaIVA
        );

        pedido.restaurarConfirmacionHistorica(pago, factura);

        return pedido;
    }

    public ClienteOnline getClienteOnline(){return clienteOnline;}
    public Direccion getDireccionEnvio(){return direccionEnvio;}
    public EstadoPedido getEstadoPedido(){return estadoPedido;}

    @Override
    public boolean confirmar(Pago pago, Factura factura){
        if(estaConfirmada() || getDetalles().isEmpty()){
            return false;}

        if(!clienteOnline.puedeComprarEnLinea()){
            return false;}

        if(pago == null || factura == null){
            return false;}

        if(!preciosVigentes()){
            return false;}

        if(pago.getEstado() != EstadoPago.PAGADO){
            return false;}

        if(factura.getVenta() != this){
            return false;}

        if(Math.abs(pago.getMonto() - factura.calcularTotal()) > 0.01){
            return false;}

        for(DetalleVenta detalle : getDetalles()){
            if(!detalle.getProducto().puedeVenderse(detalle.getCantidad())){
                return false;}
        }

        for(DetalleVenta detalle : getDetalles()){
            detalle.getProducto().reducirStock(detalle.getCantidad());}

        establecerPago(pago);
        establecerFactura(factura);

        this.estadoPedido = EstadoPedido.EMPAQUETANDOSE;

        clienteOnline.getCarrito().vaciar();

        return true;
    }

    public boolean cambiarEstado(EstadoPedido nuevoEstado, Empleado empleado){
        if(nuevoEstado == null || empleado == null){
            return false;}

        if(!estaConfirmada() || estaFinalizado()){
            return false;}

        if(!empleado.puedeRealizar(OperacionEmpleado.GESTIONAR_PEDIDOS)){
            return false;}

        if(estadoPedido == EstadoPedido.EMPAQUETANDOSE
                && nuevoEstado == EstadoPedido.EN_CAMINO){

            estadoPedido = EstadoPedido.EN_CAMINO;
            return true;}

        if(estadoPedido == EstadoPedido.EN_CAMINO
                && nuevoEstado == EstadoPedido.ENTREGADO){

            estadoPedido = EstadoPedido.ENTREGADO;
            return true;}

        return false;
    }

    public boolean puedeCancelar(){
        return estaConfirmada()
                && estadoPedido == EstadoPedido.EMPAQUETANDOSE;
    }

    public boolean cancelar(){
        if(!puedeCancelar()){
            return false;
        }

        Pago pago = getPago();

        if(pago == null || !pago.reembolsar()){
            return false;
        }

        for(DetalleVenta detalle : getDetalles()){
            detalle.getProducto().aumentarStock(detalle.getCantidad());
        }

        this.estadoPedido = EstadoPedido.CANCELADO;

        return true;
    }

    public boolean estaFinalizado(){
        return estadoPedido == EstadoPedido.ENTREGADO
                || estadoPedido == EstadoPedido.CANCELADO;
    }
}